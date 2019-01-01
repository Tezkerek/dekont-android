package ro.ande.dekont.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
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
        mApplication: Application,
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository
) : AndroidViewModel(mApplication) {
    val transactions: LiveData<Resource<List<Transaction>>>
        get() = _transactions
    var currentPage: Int = 0

    val categories: LiveData<Resource<List<Category>>>
        get() = _categories

    val transactionsWithCategories: LiveData<Pair<Resource<List<Transaction>>, Resource<List<Category>>>>
        get() = _transactionsWithCategories

    private val _transactions = MediatorLiveData<Resource<List<Transaction>>>()
    private val _categories = MediatorLiveData<Resource<List<Category>>>()
    private val _transactionsWithCategories = MediatorLiveData<Pair<Resource<List<Transaction>>, Resource<List<Category>>>>()

    fun loadTransactions(page: Int, users: List<Int>? = null) {
        _transactions.addSource(transactionRepository.loadTransactions(page, users)) {
            _transactions.value = it
            currentPage = page
        }
    }

    /** Same as loadTransactions, but only emits when both transactions and categories are loaded. */
    fun loadTransactionsWithCategories(page: Int, users: List<Int>? = null) {
        val bothLiveData = zipLiveData(transactions, categories)
        loadTransactions(page, users)
        loadCategories()

        _transactionsWithCategories.addSource(bothLiveData) {
            _transactionsWithCategories.value = it
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