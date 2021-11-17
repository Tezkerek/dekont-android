package ro.ande.dekont.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.liveData
import ro.ande.dekont.repo.TransactionRepository
import ro.ande.dekont.vo.ResourceDeletion
import ro.ande.dekont.vo.Transaction
import javax.inject.Inject

class TransactionDetailViewModel
@Inject constructor(app: Application, private val transactionRepository: TransactionRepository) :
    AndroidViewModel(app) {
    val transaction: LiveData<Transaction>
        get() = _transaction

    private val _transaction = MediatorLiveData<Transaction>()

    fun loadTransaction(id: Int) {
        _transaction.addSource(transactionRepository.retrieveTransactionById(id)) { transaction ->
            _transaction.value = transaction
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