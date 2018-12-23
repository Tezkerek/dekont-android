package ro.ande.dekont.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import ro.ande.dekont.repo.TransactionRepository
import ro.ande.dekont.repo.UserRepository
import ro.ande.dekont.ui.LoginActivity
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.ResourceDeletion
import ro.ande.dekont.vo.Transaction
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class MainViewModel
@Inject constructor(
        val mApplication: Application,
        val userRepository: UserRepository
) : AndroidViewModel(mApplication) {
    val isLoginValid: LiveData<Boolean>
        get() = _isLoginValid

    private val _isLoginValid: MediatorLiveData<Boolean> = MediatorLiveData()

    private val authToken: String? by lazy {
        mApplication.getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", null)
    }

    fun verifyLogin() {
        _isLoginValid.value = authToken != null
    }

    fun logout() {
        userRepository.logout().also {
            _isLoginValid.addSource(it) { response ->
                // Delete token and invalidate login
                mApplication.getSharedPreferences(LoginActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                        .edit()
                        .remove(LoginActivity.SHARED_PREFERENCES_TOKEN_KEY)
                        .apply()

                _isLoginValid.value = false
                _isLoginValid.removeSource(it)
            }
        }
    }
}