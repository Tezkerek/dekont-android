package ro.ande.dekont.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import ro.ande.dekont.repo.CategoryRepository
import ro.ande.dekont.repo.TransactionRepository
import ro.ande.dekont.util.NetworkState
import ro.ande.dekont.util.PagedList
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
    val transactions: LiveData<PagedList<Transaction>>
        get() = _transactions

    val transactionsState: LiveData<NetworkState>
        get() = _transactionsState

    var transactionsLastLoadedPage: Int = 0

    val categories: LiveData<Resource<List<Category>>>
        get() = _categories

    val transactionsWithCategories: LiveData<Pair<PagedList<Transaction>, Resource<List<Category>>>>
        get() = _transactionsWithCategories


    private val _transactions = MediatorLiveData<PagedList<Transaction>>()
    private val _transactionsState = MediatorLiveData<NetworkState>()
    private val _categories = MediatorLiveData<Resource<List<Category>>>()
    private val _transactionsWithCategories = MediatorLiveData<Pair<PagedList<Transaction>, Resource<List<Category>>>>()

    fun loadTransactions(page: Int, users: List<Int>? = null) {
        transactionRepository.loadTransactions(page, users).let {
            _transactions.addSource(it.data) { transactions ->
                // Update page with the new data
                _transactions.run {
                    value = (value ?: PagedList()).apply { setPageContents(page, transactions) }
                }
            }

            _transactionsState.addSource(it.networkState) { state ->
                _transactionsState.value = state
            }
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