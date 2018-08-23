package ro.ande.dekont

import org.json.JSONObject
import java.net.HttpURLConnection

class DekontResponse(connection: HttpURLConnection) {
    val statusCode = connection.responseCode
    val success = statusCode.toString()[0] == '2'
    var json: JSONObject? = null
    var errors: JSONObject? = null

    init {
        if (success) {
            // Status code 2xx
            val jsonStr = connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
            this.json = JSONObject(jsonStr)
        } else {
            val jsonStr = connection.errorStream.use { it.reader().use { reader -> reader.readText() } }
            this.errors = JSONObject(jsonStr)
        }
    }
}

class LoginResponse {
    var token: String? = null
}