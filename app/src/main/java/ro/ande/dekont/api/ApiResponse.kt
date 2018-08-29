package ro.ande.dekont.api

import retrofit2.Response
import java.io.IOException

/**
 * Represents an API response.
 * T is the type of the body.
 */
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

                ApiErrorResponse(errorMsg ?: "Unknown error")
            }
        }

        fun <T> create(error: Throwable): ApiErrorResponse<T> {
            return ApiErrorResponse(error.message ?: "Unknown error")
        }
    }
}

/**
 * Represents an empty response.
 */
class ApiEmptyResponse<T> : ApiResponse<T>()

class ApiSuccessResponse<T>(val body: T) : ApiResponse<T>()

class ApiErrorResponse<T>(val errorMessage: String) : ApiResponse<T>()