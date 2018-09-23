package ro.ande.dekont.repo

import androidx.lifecycle.LiveData
import org.threeten.bp.LocalDate
import ro.ande.dekont.AppExecutors
import ro.ande.dekont.api.DekontService
import ro.ande.dekont.api.ApiResponse
import ro.ande.dekont.db.TransactionDao
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.Transaction
import javax.inject.Inject

class TransactionRepository
@Inject constructor(
        private val appExecutors: AppExecutors,
        private val transactionDao: TransactionDao,
        private val dekontService: DekontService
) {
    fun loadTransactions(users: List<Int>?): LiveData<Resource<List<Transaction>>> {
        return object : NetworkBoundResource<List<Transaction>, List<Transaction>>(appExecutors) {
            override fun saveCallResult(result: List<Transaction>) = transactionDao.insertAndReplace(result)

            override fun shouldFetch(data: List<Transaction>?): Boolean = true

            override fun loadFromDb(): LiveData<List<Transaction>> = transactionDao.retrieveSince(LocalDate.now().withDayOfMonth(1))

            override fun createCall(): LiveData<ApiResponse<List<Transaction>>> = dekontService.listTransactions(users)
        }.asLiveData()
    }
}