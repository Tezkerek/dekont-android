package ro.ande.dekont.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.doAsync
import ro.ande.dekont.AppExecutors
import ro.ande.dekont.api.*
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.Token
import ro.ande.dekont.vo.User
import javax.inject.Inject

class UserRepository @Inject constructor(
        private val appExecutors: AppExecutors,
        private val dekontService: DekontService
) {

    fun login(email: String, password: String, name: String): Deferred<ApiResponse<Token>> {
        val loginRequest = LoginRequest(email, password, name)

        return dekontService.login(loginRequest)
    }

    fun register(email: String, password: String): Deferred<ApiResponse<User>> {
        return dekontService.register(RegistrationRequest(email, password, null))
    }

    fun logout(): LiveData<ApiResponse<Void>> = dekontService.logout()

    fun verifyToken(token: Token) = verifyToken(token.token)
    fun verifyToken(token: String) = dekontService.verifyAuthToken(token)

    fun retrieveCurrentUser(): LiveData<Resource<User>> {
        val userLiveData: MutableLiveData<Resource<User>> = MutableLiveData()

        appExecutors.networkIO().doAsync {
            runBlocking {
                val response = dekontService.retrieveCurrentUser().await()
                when (response) {
                    is ApiSuccessResponse -> {
                        userLiveData.postValue(Resource.success(response.body))
                    }
                    is ApiErrorResponse -> {
                        userLiveData.postValue(Resource.error(response.getFirstError(), null))
                    }
                }
            }
        }

        return userLiveData
    }
}