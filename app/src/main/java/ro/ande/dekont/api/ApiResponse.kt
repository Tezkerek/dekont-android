package ro.ande.dekont.api

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import ro.ande.dekont.R
import java.io.IOException
import java.net.ConnectException
import kotlin.reflect.KClass

/**
 * Represents an API response.
 * T is the type of the body.
 */
@Suppress("unused")
sealed class ApiResponse<T> {
    companion object {
        fun <T> create(response: Response<T>): ApiResponse<T> {
            return if (response.isSuccessful) {
                val body = response.body()

                if (body == null || response.code() == 204) {
                    ApiEmptyResponse()
                } else {
                    ApiSuccessResponse(body)
                }
            } else {
                // Get error message
                val bodyMsg = response.errorBody()?.string()
                val errorMsg = if (bodyMsg.isNullOrEmpty()) {
                    response.message()
                } else {
                    bodyMsg
                }

                ApiErrorResponse(errorMsg)
            }
        }

        fun <T> create(error: Throwable): ApiErrorResponse<T> {
            return ApiErrorResponse.createFromThrowable(error)
        }

        fun <T> create(context: Context, error: Throwable): ApiErrorResponse<T> {
            return ApiErrorResponse.createFromThrowable(context, error)
        }
    }
}

/**
 * Represents an empty response.
 */
class ApiEmptyResponse<T> : ApiResponse<T>()

class ApiSuccessResponse<T>(val body: T) : ApiResponse<T>()

class ApiErrorResponse<T>(errorResponse: JSONObject) : ApiResponse<T>() {
    private var detail: String? = null

    private var nonFieldErrors: List<String>? = null
    private val fieldErrors: Map<String, List<String>>

    init {
        // Extract detail from the error response
        if (errorResponse.has("detail")) {
            detail = errorResponse.getString("detail")
            errorResponse.remove("detail")
        }

        val mutableFieldErrors: MutableMap<String, List<String>> = mutableMapOf()

        for (key in errorResponse.keys()) {
            val errorsList: List<String> = getListFromJsonArray(errorResponse.getJSONArray(key)!!)

            if (key == "non_field_errors") {
                // non_field_errors are stored in a separate variable
                nonFieldErrors = errorsList
            } else {
                mutableFieldErrors[key] =  errorsList
            }
        }

        fieldErrors = mutableFieldErrors
    }

    constructor(json: String?) : this(
            if (json == null) malformedResponseMessage
            else try {
                JSONObject(json)
            } catch (e: JSONException) {
                malformedResponseMessage
            }
    )

    /**
     * Retrieves the errors for the field. Returns null if no errors are found.
     */
    fun getFieldErrors(field: String): List<String>? {
        return fieldErrors[field]
    }

    fun getErrorDetail(): String = detail ?: "Unknown error"

    fun getNonFieldErrors(): List<String>? = nonFieldErrors

    fun getFirstError(): String =
            detail
            ?: nonFieldErrors?.first()
            ?: fieldErrors.entries.iterator().run { if (hasNext()) next().run { "$key: ${value.first()}" } else null }
            ?: "No error response from server"

    companion object {
        fun <T> createFromThrowable(throwable: Throwable): ApiErrorResponse<T> {
            throwable.printStackTrace()
            val message = getMessageByException(throwable).second
            return ApiErrorResponse(createSingleMessage(message))
        }

        fun <T> createFromThrowable(context: Context, throwable: Throwable): ApiErrorResponse<T> {
            val message = context.getString(getMessageByException(throwable).first)
            return ApiErrorResponse(createSingleMessage(message))
        }

        /** A mapping of exception classes to error messages */
        private val exceptionToMessageMap = mapOf<KClass<out Throwable>, Pair<Int, String>>(
                IOException::class to Pair(R.string.error_network, "Network error"),
                ConnectException::class to Pair(R.string.error_server_unreachable, "Unable to reach server")
        )

        /**
         * Helper function for obtaining the corresponding error message from an exception.
         * @return A Pair of resIds and strings, where the strings are to be used if no context is available.
         */
        private fun getMessageByException(exception: Throwable): Pair<Int, String> =
                exceptionToMessageMap[exception::class]
                        ?: Pair(R.string.error_unknown, "Unknown error")

        private fun <T> getListFromJsonArray(array: JSONArray): List<T> {
            val mutableNonFieldErrors = mutableListOf<T>()

            for (i in 0 until array.length()) {
                mutableNonFieldErrors.add(array.get(i) as T)
            }

            return mutableNonFieldErrors
        }

        private fun createSingleMessage(message: String, key: String = "detail"): JSONObject {
            val converted = when (key) {
                "detail" -> message
                else -> JSONArray(listOf(message))
            }
            return JSONObject().put(key, converted)
        }

        private val malformedResponseMessage = createSingleMessage("Unknown error: server response is malformed")
    }
}