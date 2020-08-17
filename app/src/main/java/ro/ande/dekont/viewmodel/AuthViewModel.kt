package ro.ande.dekont.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ro.ande.dekont.api.ApiErrorResponse
import ro.ande.dekont.api.ApiResponse
import ro.ande.dekont.api.ApiSuccessResponse
import ro.ande.dekont.repo.UserRepository
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.Token
import ro.ande.dekont.vo.User
import javax.inject.Inject
import kotlin.math.ceil

class AuthViewModel
@Inject constructor(app: Application, private val userRepository: UserRepository) : AndroidViewModel(app) {
    val authToken: LiveData<Resource<Token>>
        get() = mediatorAuthToken
    val registrationResponse: LiveData<ApiResponse<User>>
        get() = _registrationResponse

    private val mediatorAuthToken = MediatorLiveData<Resource<Token>>()
    private val _registrationResponse = MutableLiveData<ApiResponse<User>>()

    fun attemptLogin(email: String, password: String) {
        // Generate a random number to identify device
        val deviceName = "android-" + ceil(Math.random() * 100)

        viewModelScope.launch {
            val response = userRepository.login(email, password, deviceName)
            when (response) {
                is ApiSuccessResponse -> mediatorAuthToken.value = Resource.success(response.body)
                is ApiErrorResponse -> mediatorAuthToken.value = Resource.error(response.getFirstError(), null)
            }
        }
    }

    fun attemptRegistration(email: String, password: String) {
        viewModelScope.launch {
            val response = userRepository.register(email, password)
            _registrationResponse.value = response
        }
    }
}