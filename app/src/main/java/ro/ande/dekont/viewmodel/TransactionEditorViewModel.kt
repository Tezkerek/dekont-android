package ro.ande.dekont.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ro.ande.dekont.repo.CategoryRepository
import ro.ande.dekont.repo.TransactionRepository
import ro.ande.dekont.vo.Category
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.Transaction
import javax.inject.Inject

class TransactionEditorViewModel
@Inject constructor(
        app: Application,
        private val transactionRepository: TransactionRepository,
        private val categoryRepository: CategoryRepository
): AndroidViewModel(app) {
    val categories: LiveData<List<Category>> = categoryRepository.retrieveAll().asLiveData()

    val currencies: LiveData<List<String>> by lazy {
        val liveData = MutableLiveData<List<String>>()

        // Retrieve currencies from the app's assets
        viewModelScope.launch(Dispatchers.IO) {
            val currencies = app.assets.open("currencies.csv")
                    .bufferedReader()
                    .use { it.readText() }
                    .split('\n')
            liveData.postValue(currencies)
        }

        liveData
    }

    val transactionResource: LiveData<Resource<Transaction>>
        get() = _transactionResource

    private val _transactionResource = MediatorLiveData<Resource<Transaction>>()

    fun createTransaction(transaction: Transaction) {
        viewModelScope.launch {
            val resource = transactionRepository.createTransaction(transaction)
            _transactionResource.value = resource
        }
    }

    fun saveTransaction(transaction: Transaction) {
        TODO("Implement update")
//        val transaction = transactionRepository.updateTransaction(transaction)
    }
}