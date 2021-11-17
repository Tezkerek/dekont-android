package ro.ande.dekont.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ro.ande.dekont.repo.TransactionRepository
import ro.ande.dekont.vo.ResourceDeletion
import ro.ande.dekont.vo.Transaction
import javax.inject.Inject

class TransactionDetailViewModel
@Inject constructor(app: Application, private val transactionRepository: TransactionRepository) :
    AndroidViewModel(app) {

    private val _transaction = MutableStateFlow<Transaction?>(null)
    val transaction: StateFlow<Transaction?>
        get() = _transaction

    fun loadTransaction(id: Int) {
        viewModelScope.launch {
            _transaction.value = transactionRepository.retrieveTransactionById(id)
        }
    }

    fun deleteCurrentTransaction(): LiveData<ResourceDeletion> =
        liveData {
            transaction.value?.let {
                val deletion = transactionRepository.deleteTransaction(it.id)
                emit(deletion)
            }
        }
}