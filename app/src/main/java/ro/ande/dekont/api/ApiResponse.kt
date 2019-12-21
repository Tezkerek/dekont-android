package ro.ande.dekont.api

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import ro.ande.dekont.R
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import kotlin.reflect.KClass

/**
 * Represents an API response.
 * T is the type of the body.
 */
@Suppress("unused")
sealed class ApiResponse<T> {
    fun isSuccess() = this is ApiSuccessResponse || this is ApiEmptyResponse

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
    val errors: ApiErrors

    init {
        // Extract detail from the error response
        val detail = errorResponse.optString("detail", null)
        if (detail != null) errorResponse.remove("detail")

        // Extract non-field errors
        val nonFieldErrors: List<String> = errorResponse.optJSONArray("non_field_errors").let {
            if (it == null) listOf()
            else {
                errorResponse.remove("non_field_errors")
                getListFromJsonArray(it)
            }
        }

        // Collect remaining errors in a map
        val fieldErrors = errorResponse.keys().asSequence().associate { field ->
            field to getListFromJsonArray<String>(errorResponse.getJSONArray(field))
        }.toMap()

        errors = ApiErrors(detail, fieldErrors, nonFieldErrors)
    }

    constructor(json: String?) : this(
            if (json == null) malformedResponseMessage
            else try {
                JSONObject(json)
            } catch (e: JSONException) {
                malformedResponseMessage
            }
    )

    fun getFirstError(): String = errors.getFirstError()

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
                ConnectException::class to Pair(R.string.error_server_unreachable, "Unable to reach server"),
                SocketTimeoutException::class to Pair(R.string.error_server_unreachable, "Unable to reach server")
        )

        /**
         * Helper function for obtaining the corresponding error message from an exception.
         * @return A Pair of resIds and strings, where the strings are to be used if no context is available.
         */
        private fun getMessageByException(exception: Throwable): Pair<Int, String> {
            Log.e("ApiResponse", "Received exception: ${exception.javaClass} ${exception.message}")
            return exceptionToMessageMap[exception::class]
                    ?: Pair(R.string.error_unknown, "Unknown error")
        }

        /**
         * Collects all of the [JSONArray]'s elements of type [T] into a list.
         * @param T The type of the elements to collect
         * @param array
         */
        private inline fun <reified T> getListFromJsonArray(array: JSONArray): List<T> {
            val mutableNonFieldErrors = mutableListOf<T>()

            for (i in 0 until array.length()) {
                array.get(i).let {
                    if (it is T) {
                        mutableNonFieldErrors.add(array.get(i) as T)
                    }
                }
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

class ApiErrors(
        val detail: String?,
        val fieldErrors: Map<String, List<String>>,
        val nonFieldErrors: List<String>
) {
    fun getFirstError(): String =
            detail
                    ?: nonFieldErrors.firstOrNull()
                    ?: fieldErrors.entries.iterator().run { if (hasNext()) next().run { "$key: ${value.first()}" } else null }
                    ?: "No error response from server"
}