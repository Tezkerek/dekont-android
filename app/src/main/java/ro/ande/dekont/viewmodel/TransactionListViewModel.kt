package ro.ande.dekont.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import ro.ande.dekont.repo.TransactionRepository
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.ResourceDeletion
import ro.ande.dekont.vo.Transaction
import javax.inject.Inject

class TransactionListViewModel
@Inject constructor(
        val mApplication: Application,
        val transactionRepository: TransactionRepository
) : AndroidViewModel(mApplication) {
    val transactions: LiveData<Resource<List<Transaction>>>
        get() = _transactions

    private val _transactions = MediatorLiveData<Resource<List<Transaction>>>()

    fun loadTransactions(users: List<Int>? = null) {
        _transactions.addSource(transactionRepository.loadTransactions(users)) {
            _transactions.value = it
        }
    }

    fun deleteTransaction(id: Int): LiveData<ResourceDeletion> {
        return transactionRepository.deleteTransaction(id)
    }
}