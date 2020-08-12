package ro.ande.dekont.ui

import android.app.DatePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.coordinatorlayout.widget.CoordinatorLayout
import kotlinx.android.synthetic.main.view_transaction_editor_form.view.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import ro.ande.dekont.R
import ro.ande.dekont.vo.Category
import ro.ande.dekont.vo.Transaction
import java.math.BigDecimal
import java.util.*

class TransactionEditorForm(context: Context, attrs: AttributeSet) : CoordinatorLayout(context, attrs) {
    init {
        LayoutInflater.from(context).inflate(R.layout.view_transaction_editor_form, this)

        date_view.setOnClickListener { openDatePicker() }
        save_button.setOnClickListener { submitTransaction() }
    }

    var date: LocalDate = LocalDate.now()
        set(value) {
            field = value
            date_view.text = value.format(DateTimeFormatter.ISO_LOCAL_DATE)
        }

    fun setCurrencies(currencies: List<String>) {
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, currencies)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val localCurrencyCode = Currency.getInstance(Locale.getDefault()).currencyCode
        val localCurrencyPosition = adapter.getPosition(localCurrencyCode)

        currency_spinner.adapter = adapter
        currency_spinner.setSelection(localCurrencyPosition)
    }

    fun setCategories(categories: List<Category>) {
        val choices = listOf(Category(0, "No category")) + categories

        category_spinner.adapter =
                CategoriesAdapter(context, android.R.layout.simple_spinner_item, choices).also {
//                    it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
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
        date = LocalDate.of(year, month+1, day)
        onDateSelectedListener?.onDateSelected(date)
    }

    private fun openDatePicker() {
        val picker = DatePickerDialog(
                context,
                R.style.DatePickerDialog,
                onDateSetListener,
                date.year,
                date.monthValue-1,
                date.dayOfMonth
        )
        picker.show()
    }

    private fun submitTransaction() {
        val transaction = Transaction(
                date,
                amount = BigDecimal(this.amount_input.text.toString().let { if (it.isEmpty()) "0" else it }),
                currency = Currency.getInstance(this.currency_spinner.selectedItem.toString()),
                categoryId = category_spinner.selectedItemId.let { if (it == 0L) null else it }?.toInt(),
                description = this.description_input.text.toString(),
                supplier = this.supplier_input.text.toString(),
                documentType = this.document_type_input.text.toString(),
                documentNumber = this.document_number_input.text.toString()
        )

        onTransactionSaveListener?.onTransactionSave(transaction)
    }


    private class CategoriesAdapter(context: Context, resource: Int, categories: List<Category>) : ArrayAdapter<Category>(context, resource, categories) {
        override fun getItemId(position: Int): Long = getItem(position)!!.id.toLong()
    }
}