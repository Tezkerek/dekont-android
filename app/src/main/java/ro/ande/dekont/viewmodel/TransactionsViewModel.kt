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

class TransactionsViewModel
@Inject constructor(
        val mApplication: Application,
        val transactionRepository: TransactionRepository
) : AndroidViewModel(mApplication) {
    val transactions: LiveData<Resource<List<Transaction>>>
        get() = _transactions

    val isLoginValid: MutableLiveData<Boolean> = MutableLiveData()

    private var _snackbarMessage: MediatorLiveData<String?> = MediatorLiveData()
    val snackbarMessage: LiveData<String?>
        get() = _snackbarMessage

    private val _transactions = MediatorLiveData<Resource<List<Transaction>>>()

    private val authToken: String? by lazy {
        mApplication.getSharedPreferences("auth", Context.MODE_PRIVATE).getString("token", null)
    }

    fun verifyLogin() {
        isLoginValid.value = authToken != null
    }

    private val loadedTransactions = AtomicBoolean(false)
    fun loadTransactions(users: List<Int>? = null) {
        _transactions.addSource(transactionRepository.loadTransactions(users)) {
            _transactions.value = it
        }
        if (loadedTransactions.compareAndSet(false, true)) {
            _snackbarMessage.addSource(transactions) {
                if (it.isError()) {
                    _snackbarMessage.value = it.message
                    _snackbarMessage.value = null
                }
            }
        }
    }

    fun deleteTransaction(id: Int): LiveData<ResourceDeletion> {
        return transactionRepository.deleteTransaction(id)
    }
}