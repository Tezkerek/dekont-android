package ro.ande.dekont.viewmodel

import android.app.Application
import android.arch.lifecycle.*
import ro.ande.dekont.api.ApiErrorResponse
import ro.ande.dekont.api.ApiSuccessResponse
import ro.ande.dekont.repo.UserRepository
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.Token
import javax.inject.Inject

class LoginViewModel
@Inject constructor(val mApplication: Application, val userRepository: UserRepository) : AndroidViewModel(mApplication) {
    val authToken: LiveData<Resource<Token>>
        get() = mediatorAuthToken

    private val mediatorAuthToken = MediatorLiveData<Resource<Token>>()

    fun attemptLogin(email: String, password: String, name: String) {
        val login = userRepository.login(email, password, name)

        mediatorAuthToken.addSource(login) { response ->
            when (response) {
                is ApiSuccessResponse -> mediatorAuthToken.value = Resource.success(response.body)
                is ApiErrorResponse -> mediatorAuthToken.value = Resource.error(response.getFirstError(), null)
            }
        }
    }
}