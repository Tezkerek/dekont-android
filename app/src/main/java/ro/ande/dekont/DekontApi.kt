package ro.ande.dekont

import android.content.Context
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

const val BASE_URL = "http://192.168.0.52:8000"

private fun performJsonRequest(method: String, urlPart: String, jsonData: JSONObject? = null, token: String? = null): DekontResponse {
    val url = URL("$BASE_URL/$urlPart/")

    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = method.toUpperCase()
    connection.addRequestProperty("Content-Type", "application/json")

    // Add auth token if given
    if (token != null) {
        connection.addRequestProperty("Authorization", "Token $token")
    }

    // Add JSON data if given
    if (jsonData != null) {
        connection.doOutput = true
        OutputStreamWriter(connection.outputStream).use { it.write(jsonData.toString()) }
    }

    connection.connect()

    return DekontResponse(connection)
}

class DekontApi constructor(private val context: Context) {
    val authToken: String? by lazy {
        this.context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString(SHARED_PREFERENCES_TOKEN_KEY, null)
    }

    fun performRequest(method: String, urlPart: String, jsonData: JSONObject): DekontResponse {
        return performJsonRequest(method, urlPart, jsonData, this.authToken)
    }

    fun isLoggedIn() = authToken != null
    fun isLoginValid() = this.isLoggedIn() && verifyAuthTokenValidity(this.authToken!!)

    companion object {
        const val SHARED_PREFERENCES_NAME = "auth"
        const val SHARED_PREFERENCES_TOKEN_KEY = "token"

        /**
         * Attempt login.
         */
        fun login(email: String, password: String, name: String): LoginResponse {
            val jsonData = JSONObject(mapOf(
                    "email" to email,
                    "password" to password,
                    "name" to name
            ))

            val response = performJsonRequest("POST", "login", jsonData)
            return LoginResponse(response)
        }

        /**
         * Verify validity of auth token.
         */
        fun verifyAuthTokenValidity(authToken: String): Boolean {
            val response = performJsonRequest("GET", "verify-authtoken/$authToken")

            return response.json!!.getBoolean("is_valid")
        }
    }
}