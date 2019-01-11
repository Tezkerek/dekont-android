package ro.ande.dekont.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
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

class AuthViewModel
@Inject constructor(app: Application, private val userRepository: UserRepository) : AndroidViewModel(app) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    val authToken: LiveData<Resource<Token>>
        get() = mediatorAuthToken
    val registrationResponse: LiveData<ApiResponse<User>>
        get() = _registrationResponse

    private val mediatorAuthToken = MediatorLiveData<Resource<Token>>()
    private val _registrationResponse = MutableLiveData<ApiResponse<User>>()

    fun attemptLogin(email: String, password: String) {
        // Generate a random number to identify device
        val deviceName = "android-" + Math.ceil(Math.random() * 100)

        val login = userRepository.login(email, password, deviceName)

        mediatorAuthToken.addSource(login) { response ->
            when (response) {
                is ApiSuccessResponse -> mediatorAuthToken.value = Resource.success(response.body)
                is ApiErrorResponse -> mediatorAuthToken.value = Resource.error(response.getFirstError(), null)
            }
        }
    }

    fun attemptRegistration(email: String, password: String) {
        coroutineScope.launch {
            val response = userRepository.register(email, password).await()

            _registrationResponse.value = response
        }
    }
}