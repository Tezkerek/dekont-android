package ro.ande.dekont.api

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

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

                ApiErrorResponse(errorMsg)
            }
        }

        fun <T> create(error: Throwable): ApiErrorResponse<T> {
            return ApiErrorResponse.createFromThrowable(error)
        }
    }
}

/**
 * Represents an empty response.
 */
class ApiEmptyResponse<T> : ApiResponse<T>()

class ApiSuccessResponse<T>(val body: T) : ApiResponse<T>()

class ApiErrorResponse<T>(val errorResponse: JSONObject) : ApiResponse<T>() {
    private var detail: String? = null

    private var nonFieldErrors: List<String>? = null
    private val fieldErrors: Map<String, List<String>>

    init {
        if (errorResponse.has("detail")) {
            detail = errorResponse.getString("detail")
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
            return ApiErrorResponse(createSingleMessage(throwable.message ?: "Unknown error"))
        }

        private fun <T> getListFromJsonArray(array: JSONArray): List<T> {
            val mutableNonFieldErrors = mutableListOf<T>()

            for (i in 0 until array.length()) {
                mutableNonFieldErrors.add(array.get(i) as T)
            }

            return mutableNonFieldErrors
        }

        private fun createSingleMessage(message: String, key: String = "detail"): JSONObject = JSONObject().put(key, JSONArray(listOf(message)))

        private val malformedResponseMessage = createSingleMessage("Unknown error: server response is malformed")
    }
}