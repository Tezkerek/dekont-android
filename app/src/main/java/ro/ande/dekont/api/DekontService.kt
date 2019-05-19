package ro.ande.dekont.api

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import ro.ande.dekont.vo.*

interface DekontService {
    @POST("login/")
    fun login(@Body body: LoginRequest): Deferred<ApiResponse<Token>>

    @POST("register/")
    fun register(@Body body: RegistrationRequest): Deferred<ApiResponse<User>>

    @POST("logout/")
    fun logout(): LiveData<ApiResponse<Void>>

    @GET("verify-authtoken/{token}")
    fun verifyAuthToken(@Path("token") token: String): Call<ResponseBody>

    @GET("current-user/")
    fun retrieveCurrentUser(): Deferred<ApiResponse<User>>

    @GET("current-group/")
    fun retrieveCurrentUserGroup(): Deferred<ApiResponse<Group>>

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
