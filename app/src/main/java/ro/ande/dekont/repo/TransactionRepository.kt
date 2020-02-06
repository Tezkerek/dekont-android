package ro.ande.dekont.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import org.jetbrains.anko.doAsync
import org.threeten.bp.LocalDate
import ro.ande.dekont.AppExecutors
import ro.ande.dekont.api.ApiErrorResponse
import ro.ande.dekont.api.ApiSuccessResponse
import ro.ande.dekont.api.DekontService
import ro.ande.dekont.db.TransactionDao
import ro.ande.dekont.util.LoadMoreLiveData
import ro.ande.dekont.util.NetworkState
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.ResourceDeletion
import ro.ande.dekont.vo.Transaction
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class TransactionRepository
@Inject constructor(
        private val appExecutors: AppExecutors,
        private val transactionDao: TransactionDao,
        private val dekontService: DekontService
) {
    /** Retrieve the cached page, and simultaneously fetch changes from the server. */
    fun loadTransactions(page: Int, users: List<Int>?): LoadMoreLiveData<List<Transaction>> {
        val cachedData = transactionDao.retrievePartial((page-1) * PAGE_SIZE, PAGE_SIZE)

        val networkState = liveData(Dispatchers.IO) {
            // Emit loading state
            emit(NetworkState(NetworkState.Status.LOADING, isExhausted = false))

            // Retrieve data from server
            val response = dekontService.listTransactions(page, users)
            when (response) {
                is ApiSuccessResponse -> {
                    val isDataExhausted = response.body.page == response.body.pageCount
                    emit(NetworkState(NetworkState.Status.SUCCESS, isExhausted = isDataExhausted))

                    // The DB LiveData will refresh automatically after insertion
                    transactionDao.insertAndReplace(response.body.data)
                }
                is ApiErrorResponse -> {
                    emit(NetworkState(NetworkState.Status.ERROR, message = response.getFirstError()))
                }
            }
        }

        return LoadMoreLiveData(cachedData, networkState)
    }

    fun getTransactionById(id: Int): Transaction {
        return transactionDao.getById(id)
    }

    fun retrieveTransactionById(id: Int): LiveData<Transaction> {
        return transactionDao.retrieveById(id)
    }

    fun createTransaction(
            date: LocalDate,
            amount: BigDecimal,
            currency: Currency,
            categoryId: Int?,
            description: String,
            supplier: String,
            documentType: String,
            documentNumber: String
    ): LiveData<Resource<Transaction>> {
        val transaction = Transaction(date, amount, currency, categoryId, description, supplier, documentType, documentNumber)

        // Attempt to insert on server.
        return Transformations.map(dekontService.createTransaction(transaction)) { response ->
            when (response) {
                is ApiSuccessResponse -> {
                    // Insert locally.
                    val newTransaction = response.body
                    doAsync {
                        transactionDao.insert(newTransaction)
                    }

                    Resource.success(newTransaction)
                }
                is ApiErrorResponse -> Resource.error(response.getFirstError(), null)
                else -> Resource.error("Unexpected error: response is empty", null)
            }
        }
    }

    fun updateTransaction(
            id: Int,
            date: LocalDate,
            amount: BigDecimal,
            currency: Currency,
            description: String,
            supplier: String,
            documentType: String,
            documentNumber: String
    ): Transaction {
        val transaction = getTransactionById(id)
        transaction.also {
            it.date = date
            it.amount = amount
            it.currency = currency
            it.description = description
            it.supplier = supplier
            it.documentType = documentType
            it.documentNumber = documentNumber
        }

        transactionDao.update(transaction)

        return transaction
    }

    fun deleteTransaction(id: Int): LiveData<ResourceDeletion> {
        // Attempt to delete on server.
        return Transformations.map(dekontService.deleteTransaction(id)) { response ->
            if (response.isSuccess()) {
                // Delete locally
                doAsync {
                    transactionDao.delete(id)
                }
                ResourceDeletion.success()
            } else {
                response as ApiErrorResponse
                ResourceDeletion.error(response.getFirstError())
            }
        }
    }

    companion object {
        const val PAGE_SIZE = 15
    }
}