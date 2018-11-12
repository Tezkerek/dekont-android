package ro.ande.dekont.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.threeten.bp.LocalDate
import javax.inject.Inject

class TransactionEditorViewModel
@Inject constructor(): ViewModel() {
    val date: LiveData<LocalDate>
        get() = _date

    private val _date = MutableLiveData<LocalDate>()

    fun setDate(date: LocalDate) {
        _date.value = date
    }
}