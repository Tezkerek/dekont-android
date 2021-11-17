package ro.ande.dekont.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.view_transaction_editor_form.view.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import ro.ande.dekont.R
import ro.ande.dekont.util.IdTextPair
import ro.ande.dekont.util.IdTextPairAdapter
import ro.ande.dekont.vo.Category
import ro.ande.dekont.vo.Transaction
import java.math.BigDecimal
import java.util.*

class TransactionEditorForm(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs) {
    var date: LocalDate = LocalDate.now()
        set(value) {
            field = value
            updateDateButtonText(value)
        }
    var selectedCurrency: Currency = Currency.getInstance(Locale.getDefault())
    var selectedCategoryId: Int? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_transaction_editor_form, this)

        date_picker_button.setOnClickListener { openDatePicker() }
        save_button.setOnClickListener { submitTransaction() }
        category_dropdown.setOnItemClickListener { _, _, _, id ->
            // id 0 means no category selected
            selectedCategoryId = id.toInt().let { if (it == 0) null else it }
        }
        currency_dropdown.setOnItemClickListener { parent, _, position, _ ->
            val currencyCode = parent.getItemAtPosition(position) as String
            val currency = Currency.getInstance(currencyCode)
            if (currency != null) selectedCurrency = currency
        }

        // Needed because date's custom setter is not called on initialization
        updateDateButtonText(date)
    }

    fun setCurrencies(currencies: List<String>) {
        val adapter =
            ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        currency_dropdown.setAdapter(adapter)
        currency_dropdown.setText(selectedCurrency.currencyCode, false)
    }

    fun setCategories(categories: List<Category>) {
        val defaultSelection = IdTextPair(0, "No category")
        val choices =
            listOf(defaultSelection) + categories.map { IdTextPair(it.id.toLong(), it.name) }

        category_dropdown.setAdapter(
            IdTextPairAdapter(context, android.R.layout.simple_spinner_item, choices).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        )

        category_dropdown.setText(defaultSelection.text, false)
    }


    interface OnDateSelectedListener {
        fun onDateSelected(date: LocalDate)
    }

    var onDateSelectedListener: OnDateSelectedListener? = null

    interface OnTransactionSaveListener {
        fun onTransactionSave(transaction: Transaction)
    }

    var onTransactionSaveListener: OnTransactionSaveListener? = null

    fun setOnTransactionSaveListener(l: ((Transaction) -> Unit)) {
        onTransactionSaveListener = object : OnTransactionSaveListener {
            override fun onTransactionSave(transaction: Transaction) = l(transaction)
        }
    }


    private val onDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        date = LocalDate.of(year, month + 1, day)
        onDateSelectedListener?.onDateSelected(date)
    }

    private fun updateDateButtonText(date: LocalDate) {
        date_picker_button.setText(date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
    }

    private fun openDatePicker() {
        val picker = DatePickerDialog(
            context,
            onDateSetListener,
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        )
        picker.show()
    }

    private fun submitTransaction() {
        val transaction = Transaction(
            date,
            amount = BigDecimal(amount_input.text.toString().let { if (it.isEmpty()) "0" else it }),
            currency = selectedCurrency,
            categoryId = selectedCategoryId,
            description = description_input.text.toString(),
            supplier = supplier_input.text.toString(),
            documentType = document_type_input.text.toString(),
            documentNumber = document_number_input.text.toString()
        )

        onTransactionSaveListener?.onTransactionSave(transaction)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return SavedState(superState!!).also { state ->
            state.dateEpoch = date.toEpochDay()
            state.selectedCurrency = selectedCurrency.currencyCode
            state.selectedCategory = selectedCategoryId
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)

            date = LocalDate.ofEpochDay(state.dateEpoch)
            selectedCurrency = Currency.getInstance(state.selectedCurrency)
            selectedCategoryId = state.selectedCategory
        } else
            super.onRestoreInstanceState(state)
    }

    private class SavedState : BaseSavedState {
        var dateEpoch: Long = 0
        var selectedCurrency: String = "USD"
        var selectedCategory: Int? = null

        constructor(parcelable: Parcelable) : super(parcelable)

        constructor(parcel: Parcel) : super(parcel) {
            dateEpoch = parcel.readLong()
            selectedCurrency = parcel.readString() ?: "USD"
            selectedCategory = parcel.readValue(Int::class.java.classLoader) as Int?
        }

        override fun writeToParcel(out: Parcel?, flags: Int) {
            super.writeToParcel(out, flags)
            out?.writeLong(dateEpoch)
            out?.writeString(selectedCurrency)
            out?.writeValue(selectedCategory)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)

                override fun newArray(size: Int): Array<SavedState?> =
                    arrayOfNulls(size)

            }
        }
    }
}