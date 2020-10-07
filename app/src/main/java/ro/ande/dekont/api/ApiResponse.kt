package ro.ande.dekont.api

import android.content.Context
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import ro.ande.dekont.R
import ro.ande.dekont.util.getStringOrNull
import ro.ande.dekont.util.toList
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import kotlin.reflect.KClass

/**
 * Represents an API response.
 * T is the type of the body.
 */
@Suppress("unused")
sealed class ApiResponse<T> {
    fun isSuccess() = this is ApiSuccessResponse || this is ApiEmptyResponse
    fun isError() = this is ApiErrorResponse

    companion object {
        fun <T> create(response: Response<T>): ApiResponse<T> {
            return if (response.isSuccessful) {
                val body = response.body()

                if (body == null || response.code() == 204) {
                    ApiEmptyResponse()
                } else {
                    ApiSuccessResponse(body)
                }
            } else ApiErrorResponse(response)
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

class ApiErrorResponse<T> : ApiResponse<T> {
    val type: ApiErrorType
    val errors: ApiErrors

    constructor(response: Response<*>) : super() {
        type = ApiErrorType.fromHttpCode(response.code())
        errors = parseErrors(response)
    }

    constructor(message: String) {
        type = ApiErrorType.UNKNOWN
        errors = ApiErrors(message)
    }

    /**
     * Collects the errors from the response into an [ApiErrors] object.
     */
    private fun parseErrors(response: Response<*>): ApiErrors {
        // Try to retrieve the errors in the body first,
        // and fall back to the HTTP message.
        val bodyMsg = response.errorBody()?.string()
        val errorString = if (bodyMsg.isNullOrEmpty()) response.message() else bodyMsg

        if (errorString == null)
            return ApiErrors()

        val errorJson =
                try {
                    JSONObject(errorString)
                } catch (e: JSONException) {
                    return ApiErrors()
                }

        // Extract detail from the error response
        val detail: String? = errorJson.getStringOrNull("detail")
        errorJson.remove("detail")

        // Extract non-field errors
        val nonFieldErrors: List<String> =
                errorJson.optJSONArray("non_field_errors")?.toList()
                        ?: listOf()
        errorJson.remove("non_field_errors")

        // Collect remaining errors in a map
        val fieldErrors: Map<String, List<String>> =
                errorJson.keys().asSequence().associateWith { field ->
                    errorJson.getJSONArray(field).toList()
                }

        return ApiErrors(detail, fieldErrors, nonFieldErrors)
    }

    fun getFirstError(): String = errors.getFirstError()

    companion object {
        fun <T> createFromThrowable(throwable: Throwable): ApiErrorResponse<T> {
            throwable.printStackTrace()
            val message = getMessageByException(throwable).second
            return ApiErrorResponse(message)
        }

        fun <T> createFromThrowable(context: Context, throwable: Throwable): ApiErrorResponse<T> {
            val message = context.getString(getMessageByException(throwable).first)
            return ApiErrorResponse(message)
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
    }
}

class ApiErrors(
        val detail: String? = null,
        val fieldErrors: Map<String, List<String>> = mapOf(),
        val nonFieldErrors: List<String> = listOf()
) {
    fun getFirstError(): String =
            detail
                    ?: nonFieldErrors.firstOrNull()
                    ?: fieldErrors.entries.iterator().run { if (hasNext()) next().run { "$key: ${value.first()}" } else null }
                    ?: "No error response from server"
}

enum class ApiErrorType {
    UNKNOWN,
    NOT_FOUND;

    companion object {
        fun fromHttpCode(code: Int) =
                when (code) {
                    HttpURLConnection.HTTP_NOT_FOUND -> NOT_FOUND
                    else -> UNKNOWN
                }
    }
}