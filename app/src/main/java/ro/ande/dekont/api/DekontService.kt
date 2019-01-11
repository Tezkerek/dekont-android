package ro.ande.dekont.api

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import ro.ande.dekont.vo.Category
import ro.ande.dekont.vo.Token
import ro.ande.dekont.vo.Transaction
import ro.ande.dekont.vo.User

interface DekontService {
    @POST("login/")
    fun login(@Body body: LoginRequest): LiveData<ApiResponse<Token>>

    @POST("register/")
    fun register(@Body body: RegistrationRequest): Deferred<ApiResponse<User>>

    @POST("logout/")
    fun logout(): LiveData<ApiResponse<Void>>

    @GET("verify-authtoken/{token}")
    fun verifyAuthToken(@Path("token") token: String): Call<ResponseBody>

    @GET("transactions/")
    fun listTransactions(
            @Query("page") page: Int,
            @Query("users") users: List<Int>?
    ): Deferred<ApiResponse<PaginatedResponse<List<Transaction>>>>

    @POST("transactions/")
    fun createTransaction(@Body body: Transaction): LiveData<ApiResponse<Transaction>>

    @DELETE("transactions/{id}/")
    fun deleteTransaction(@Path("id") id: Int): LiveData<ApiResponse<Void>>

    @GET("categories/")
    fun listCategories(): LiveData<ApiResponse<List<Category>>>
}
