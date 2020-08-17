package ro.ande.dekont.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ro.ande.dekont.AppExecutors
import ro.ande.dekont.api.*
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.Token
import ro.ande.dekont.vo.User
import javax.inject.Inject

class UserRepository @Inject constructor(private val dekontService: DekontService) {

    private val _currentUser = MutableLiveData<Resource<User>>()
    val currentUser: LiveData<Resource<User>> = _currentUser

    suspend fun login(email: String, password: String, deviceName: String): ApiResponse<Token> =
            dekontService.login(LoginRequest(email, password, deviceName))

    suspend fun register(email: String, password: String): ApiResponse<User> {
        return dekontService.register(RegistrationRequest(email, password, null))
    }

    suspend fun logout(): ApiResponse<Void> = dekontService.logout()

    suspend fun fetchCurrentUser() {
        val response = dekontService.retrieveCurrentUser()
        when (response) {
            is ApiSuccessResponse -> {
                _currentUser.value = Resource.success(response.body)
            }
            is ApiErrorResponse -> {
                _currentUser.value = Resource.error(response.getFirstError(), null)
            }
        }
    }
}