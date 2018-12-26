package ro.ande.dekont.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import ro.ande.dekont.repo.CategoryRepository
import ro.ande.dekont.repo.TransactionRepository
import ro.ande.dekont.util.zipLiveData
import ro.ande.dekont.vo.Category
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.ResourceDeletion
import ro.ande.dekont.vo.Transaction
import javax.inject.Inject

class TransactionListViewModel
@Inject constructor(
        private val mApplication: Application,
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository
) : AndroidViewModel(mApplication) {
    val transactions: LiveData<Resource<List<Transaction>>>
        get() = _transactions

    val categories: LiveData<Resource<List<Category>>>
        get() = _categories

    private val _transactions = MediatorLiveData<Resource<List<Transaction>>>()
    private val _categories = MediatorLiveData<Resource<List<Category>>>()

    fun loadTransactions(users: List<Int>? = null) {
        _transactions.addSource(transactionRepository.loadTransactions(users)) {
            _transactions.value = it
        }
    }

    /** Same as loadTransactions, but only emits when both transactions and categories are loaded. */
    fun loadTransactionsWithCategories(users: List<Int>? = null) {
        val bothLiveData = zipLiveData(transactions, categories)
        loadTransactions(users)
        loadCategories()

        _transactions.addSource(bothLiveData) {
            _transactions.value = it.first
            _categories.value = it.second
            _transactions.removeSource(bothLiveData)
        }
    }

    fun loadCategories() {
        _categories.addSource(categoryRepository.loadAll()) {
            _categories.value = it
        }
    }

    fun deleteTransaction(id: Int): LiveData<ResourceDeletion> {
        return transactionRepository.deleteTransaction(id)
    }
}