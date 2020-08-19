package ro.ande.dekont.repo

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ro.ande.dekont.api.ApiErrorResponse
import ro.ande.dekont.api.ApiErrorType
import ro.ande.dekont.api.ApiSuccessResponse
import ro.ande.dekont.api.DekontService
import ro.ande.dekont.db.TransactionDao
import ro.ande.dekont.util.CachedNetworkData
import ro.ande.dekont.util.NetworkState
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.ResourceDeletion
import ro.ande.dekont.vo.Transaction
import javax.inject.Inject

class TransactionRepository
@Inject constructor(
        private val transactionDao: TransactionDao,
        private val dekontService: DekontService
) {
    /** Retrieve the cached page, and simultaneously fetch changes from the server. */
    fun loadTransactions(page: Int, users: List<Int>?): CachedNetworkData<List<Transaction>> {
        val cachedData =
                transactionDao
                        .retrievePartial((page - 1) * PAGE_SIZE, PAGE_SIZE)
                        .distinctUntilChanged()

        val networkState = flow {
            // Emit loading state
            emit(NetworkState(NetworkState.Status.LOADING, isExhausted = false))

            // Retrieve data from server
            val response = dekontService.listTransactions(page, users)
            when (response) {
                is ApiSuccessResponse -> {
                    val isDataExhausted = response.body.page == response.body.pageCount
                    emit(NetworkState(NetworkState.Status.SUCCESS, isExhausted = isDataExhausted))

                    // The DB source will refresh automatically after insertion
                    transactionDao.insertAndReplace(response.body.data)
                }
                is ApiErrorResponse -> {
                    emit(NetworkState(NetworkState.Status.ERROR, message = response.getFirstError()))
                }
            }
        }.flowOn(Dispatchers.IO).distinctUntilChanged()

        return CachedNetworkData(cachedData, networkState)
    }

    fun getTransactionById(id: Int): Transaction {
        return transactionDao.getById(id)
    }

    fun retrieveTransactionById(id: Int): LiveData<Transaction> {
        return transactionDao.retrieveById(id)
    }

    suspend fun createTransaction(transaction: Transaction): Resource<Transaction> =
            withContext(Dispatchers.IO) {
                // Attempt to insert on server.
                val response = dekontService.createTransaction(transaction)

                when (response) {
                    is ApiSuccessResponse -> {
                        // Insert locally.
                        val newTransaction = response.body
                        transactionDao.insert(newTransaction)

                        Resource.success(newTransaction)
                    }
                    is ApiErrorResponse -> Resource.error(response.getFirstError(), null)
                    else -> Resource.error("Unexpected error: response is empty", null)
                }
            }

    suspend fun updateTransaction(transaction: Transaction): Transaction =
            TODO("Not implemented")

    suspend fun deleteTransaction(id: Int): ResourceDeletion =
            withContext(Dispatchers.IO) {
                // Attempt to delete on server.
                val response = dekontService.deleteTransaction(id)

                // Delete locally even if the resource was not found on the server
                val shouldDeleteLocally =
                        response is ApiErrorResponse && response.type == ApiErrorType.NOT_FOUND

                if (response.isSuccess() || shouldDeleteLocally) {
                    // Delete locally
                    transactionDao.delete(id)
                    ResourceDeletion.success()
                } else {
                    response as ApiErrorResponse
                    ResourceDeletion.error(response.getFirstError())
                }
            }

    companion object {
        const val PAGE_SIZE = 15
    }
}