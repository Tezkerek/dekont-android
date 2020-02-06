package ro.ande.dekont.api

import androidx.lifecycle.LiveData
import retrofit2.http.*
import ro.ande.dekont.vo.*

interface DekontService {
    @POST("login/")
    suspend fun login(@Body body: LoginRequest): ApiResponse<Token>

    @POST("register/")
    suspend fun register(@Body body: RegistrationRequest): ApiResponse<User>

    @POST("logout/")
    suspend fun logout(): ApiResponse<Void>

    @GET("users/0/")
    suspend fun retrieveCurrentUser(): ApiResponse<User>

    @POST("users/0/group/")
    suspend fun joinGroup(@Body inviteCode: GroupJoinRequest): ApiResponse<Void>

    @GET("current-group/")
    suspend fun retrieveCurrentUserGroup(): ApiResponse<Group>

    @GET("transactions/")
    suspend fun listTransactions(
            @Query("page") page: Int,
            @Query("users") users: List<Int>?
    ): ApiResponse<PaginatedResponse<List<Transaction>>>

    @POST("transactions/")
    fun createTransaction(@Body body: Transaction): LiveData<ApiResponse<Transaction>>

    @DELETE("transactions/{id}/")
    fun deleteTransaction(@Path("id") id: Int): LiveData<ApiResponse<Void>>

    @GET("categories/")
    fun listCategories(): LiveData<ApiResponse<List<Category>>>
}
