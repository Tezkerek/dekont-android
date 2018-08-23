package ro.ande.dekont

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DekontService {
    @POST("login/")
    fun login(@Body body: RequestBody): Call<ResponseBody>

    @GET("verify-authtoken/{token}")
    fun verifyAuthToken(@Path("token") token: String): Call<ResponseBody>
}