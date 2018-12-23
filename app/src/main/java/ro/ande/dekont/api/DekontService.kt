package ro.ande.dekont.api

import androidx.lifecycle.LiveData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import ro.ande.dekont.vo.Token
import ro.ande.dekont.vo.Transaction

interface DekontService {
    @POST("login/")
    fun login(@Body body: LoginRequest): LiveData<ApiResponse<Token>>

    @POST("logout/")
    fun logout(): LiveData<ApiResponse<Void>>

    @GET("verify-authtoken/{token}")
    fun verifyAuthToken(@Path("token") token: String): Call<ResponseBody>

    @Auth
    @GET("transactions/")
    fun listTransactions(@Query("users") users: List<Int>?): LiveData<ApiResponse<List<Transaction>>>

    @POST("transactions/")
    fun createTransaction(@Body body: Transaction): LiveData<ApiResponse<Transaction>>

    @DELETE("transactions/{id}/")
    fun deleteTransaction(@Path("id") id: Int): LiveData<ApiResponse<Void>>
}

@Target(AnnotationTarget.FUNCTION)
annotation class Auth