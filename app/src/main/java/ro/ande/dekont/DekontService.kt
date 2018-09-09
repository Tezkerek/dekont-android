package ro.ande.dekont

import androidx.lifecycle.LiveData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ro.ande.dekont.api.ApiResponse
import ro.ande.dekont.api.LoginRequest
import ro.ande.dekont.vo.Token

interface DekontService {
    @POST("login/")
    fun login(@Body body: LoginRequest): LiveData<ApiResponse<Token>>

    @GET("verify-authtoken/{token}")
    fun verifyAuthToken(@Path("token") token: String): Call<ResponseBody>
}