package ro.ande.dekont.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ro.ande.dekont.R
import ro.ande.dekont.repo.CategoryRepository
import ro.ande.dekont.repo.TransactionRepository
import ro.ande.dekont.util.*
import ro.ande.dekont.vo.Category
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

    private val _categories = MediatorLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _messages: MutableStateFlow<StringResource?> = MutableStateFlow(null)
    val messages: StateFlow<StringResource?>
        get() = _messages


    init {
        loadCategories()
    }

    private fun loadTransactions(page: Int, users: List<Int>? = null) {
        val loadSource = transactionRepository.loadTransactions(page, users)

        loadSource.data.onEach { transactions ->
            // When the page is empty, exhaust the source.
            // This can happen if there is a network error,
            // and the local source is exhausted.
            if (transactions.isEmpty()) {
                lastPagedLoadState.notifyDataExhausted()
            } else {
                // Update page with the new data
                _transactions.run {
                    val currentList = value ?: PagedList()
                    currentList.setPageContents(page, transactions)
                    postValue(currentList)
                }
            }
        }.launchIn(viewModelScope)

        loadSource.networkState.onEach { networkState ->
            if (networkState !is NetworkLoadingState)
                lastPagedLoadState.notifyLoadComplete()
            if (networkState is NetworkErrorState) {
                _messages.value = networkState.message.toStringResource(R.string.error_unknown)
            }
            if (networkState.isExhausted) lastPagedLoadState.notifyDataExhausted()
        }.launchIn(viewModelScope)
    }

    private var lastPagedLoadState: PagedLoadState = PagedLoadState(0)

    fun attemptTransactionsPageLoad(pagedLoadState: PagedLoadState): Boolean {
        // Skip if page was already loaded or if data source is exhausted
        if (pagedLoadState.page <= lastPagedLoadState.page || lastPagedLoadState.isExhausted)
            return false

        lastPagedLoadState = pagedLoadState
        loadTransactions(pagedLoadState.page)
        return true
    }

    private fun loadCategories() {
        val loadSource = categoryRepository.loadAll()

        loadSource.data.onEach { categories ->
            _categories.postValue(categories)
        }.launchIn(viewModelScope)

        loadSource.networkState.onEach { networkState ->
            if (networkState !is NetworkLoadingState)
                lastPagedLoadState.notifyLoadComplete()
            if (networkState is NetworkErrorState) {
                _messages.value = networkState.message.toStringResource(R.string.error_unknown)
            }
        }.launchIn(viewModelScope)
    }

    fun attemptTransactionDeletion(id: Int) {
        viewModelScope.launch {
            val deletion = transactionRepository.deleteTransaction(id)
            if (deletion.isSuccess) {
                _messages.value = StringResource(R.string.message_transaction_deletion_success)
            } else {
                _messages.value = deletion.message.toStringResource(R.string.error_unknown)
            }
        }
    }
}