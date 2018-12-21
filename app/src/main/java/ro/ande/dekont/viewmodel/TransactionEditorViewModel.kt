package ro.ande.dekont.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import org.threeten.bp.LocalDate
import ro.ande.dekont.repo.TransactionRepository
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.Transaction
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class TransactionEditorViewModel
@Inject constructor(app: Application, val transactionRepository: TransactionRepository): AndroidViewModel(app) {
    val date: LiveData<LocalDate>
        get() = _date
    val transactionResource: LiveData<Resource<Transaction>>
        get() = _transactionResource

    private val _date = MutableLiveData<LocalDate>()
    private val _transactionResource = MediatorLiveData<Resource<Transaction>>()

    fun setDate(date: LocalDate) {
        _date.value = date
    }

    fun createTransaction(
            amount: BigDecimal,
            currency: Currency,
            description: String,
            supplier: String,
            documentType: String,
            documentNumber: String
    ) {
        val transactionLiveData = transactionRepository.createTransaction(this.date.value!!, amount, currency, description, supplier, documentType, documentNumber)
        _transactionResource.addSource(transactionLiveData) {
            _transactionResource.value = it
        }
    }

    fun saveTransaction(
            id: Int,
            amount: BigDecimal,
            currency: Currency,
            description: String,
            supplier: String,
            documentType: String,
            documentNumber: String
    ) {
        val transaction = transactionRepository.updateTransaction(id, this.date.value!!, amount, currency, description, supplier, documentType, documentNumber)
    }
}