package ro.ande.dekont.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.launch
import ro.ande.dekont.db.DekontDatabase
import ro.ande.dekont.repo.UserRepository
import ro.ande.dekont.ui.AuthActivity
import javax.inject.Inject

class MainViewModel
@Inject constructor(
        private val mApplication: Application,
        private val dekontDatabase: DekontDatabase,
        private val userRepository: UserRepository
) : AndroidViewModel(mApplication) {
    private val authToken: String? =
            mApplication.getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", null)

    private val _isLoginValid: MediatorLiveData<Boolean> = MediatorLiveData()
    val isLoginValid: LiveData<Boolean> = _isLoginValid.apply { value = verifyLogin() }

    fun verifyLogin(): Boolean = authToken != null

    fun logout() {
        viewModelScope.launch {
            // Tell the server to delete the token
            // If the token didn't actually exist, no problem
            userRepository.logout()

            // Clear database
            viewModelScope.launch(Dispatchers.IO) {
                dekontDatabase.clearAllTables()
            }

            // Delete token and invalidate login
            mApplication.getSharedPreferences(AuthActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .remove(AuthActivity.SHARED_PREFERENCES_TOKEN_KEY)
                    .apply()
            _isLoginValid.postValue(false)
        }
    }
}