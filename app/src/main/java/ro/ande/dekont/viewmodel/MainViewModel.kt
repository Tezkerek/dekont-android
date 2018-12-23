package ro.ande.dekont.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import ro.ande.dekont.repo.TransactionRepository
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.ResourceDeletion
import ro.ande.dekont.vo.Transaction
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class MainViewModel
@Inject constructor(
        val mApplication: Application
) : AndroidViewModel(mApplication) {
    val isLoginValid: MutableLiveData<Boolean> = MutableLiveData()

    private val authToken: String? by lazy {
        mApplication.getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", null)
    }

    fun verifyLogin() {
        isLoginValid.value = authToken != null
    }
}