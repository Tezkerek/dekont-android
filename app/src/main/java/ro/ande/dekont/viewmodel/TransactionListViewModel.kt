package ro.ande.dekont.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    private val _transactions = MediatorLiveData<PagedList<Transaction>>()
    val transactions: LiveData<PagedList<Transaction>> = _transactions

    private val _transactionsState = MediatorLiveData<NetworkState>()
    val transactionsState: LiveData<NetworkState> = _transactionsState

    private val _categories = MediatorLiveData<Resource<List<Category>>>()
    val categories: LiveData<Resource<List<Category>>> = _categories

    private val _transactionsWithCategories = MediatorLiveData<Pair<PagedList<Transaction>, Resource<List<Category>>>>()
    val transactionsWithCategories: LiveData<Pair<PagedList<Transaction>, Resource<List<Category>>>> = _transactionsWithCategories

    var transactionsLastLoadedPage: Int = 0


    fun loadTransactions(page: Int, users: List<Int>? = null) {
        val loadSource = transactionRepository.loadTransactions(page, users)

        loadSource.data.onEach { transactions ->
            // When the page is empty, exhaust the source.
            // This can happen if there is a network error,
            // and the local source is exhausted.
            if (transactions.isEmpty()) {
                _transactionsState.postValue(_transactionsState.value?.also { prevState ->
                    NetworkState(prevState.state, prevState.message, isExhausted = true)
                })
            } else {
                // Update page with the new data
                _transactions.run {
                    postValue((value
                            ?: PagedList()).apply { setPageContents(page, transactions) })
                }
            }
        }.launchIn(viewModelScope)

        loadSource.networkState.onEach { state ->
            _transactionsState.value = state
        }.launchIn(viewModelScope)
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

    fun deleteTransaction(id: Int): LiveData<ResourceDeletion> =
            liveData {
                emit(transactionRepository.deleteTransaction(id))
            }
}