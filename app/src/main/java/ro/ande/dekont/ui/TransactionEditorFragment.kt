package ro.ande.dekont.ui


import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_transaction_editor.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import ro.ande.dekont.R
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.viewmodel.TransactionEditorViewModel
import ro.ande.dekont.vo.Transaction
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

/**
 * A [Fragment] for editing a [Transaction].
 *
 */
class TransactionEditorFragment : Fragment(), Injectable {
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var editorViewModel: TransactionEditorViewModel

    private lateinit var onEditFinishedListener: OnTransactionEditFinishedListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_editor, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        editorViewModel = ViewModelProviders.of(this, mViewModelFactory).get(TransactionEditorViewModel::class.java)

        onEditFinishedListener = this.activity as OnTransactionEditFinishedListener

        // Fake handle a touch event to prevent propagation to the fragment below
        // We use this instead of making the view clickable because the latter makes
        // the EditText views flash white when we click the view.
        this.view?.setOnTouchListener { _, _ -> true }

        // Observe date and update the date_view
        editorViewModel.date.observe(this, Observer { date -> setDateViewText(date) })

        // Setup data
        if (editorViewModel.date.value == null) {
            editorViewModel.setDate(LocalDate.now())
        }
        populateCurrencySpinner()
        populateCategorySpinner()

        this.date_view.setOnClickListener {
            openDatePicker()
        }

        // Observe result transaction
        editorViewModel.transactionResource.observe(this, Observer { transactionResource ->
            if (transactionResource.isSuccess()) {
                finishEditing(transactionResource.data!!)
            } else if (transactionResource.isError()) {
                Snackbar.make(this.main_inputs_layout, transactionResource.message ?: "", Snackbar.LENGTH_SHORT).show()
            }
        })

        this.save_button.setOnClickListener {
            saveTransaction()
        }
    }

    // Populates the spinner with currencies
    private fun populateCurrencySpinner() {
        // Retrieve currencies from the app's assets
        val currencies = context!!.assets.open("currencies.csv")
                .bufferedReader()
                .use { it.readText() }
                .split('\n')

        val adapter = ArrayAdapter<String>(this.context!!, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        this.currency_spinner.adapter = adapter
    }

    private fun populateCategorySpinner() {
        editorViewModel.categories.observe(this, Observer { categories ->
            val choices = listOf("No category") + categories.map { it.name }
            this.category_spinner.adapter =
                    ArrayAdapter<String>(this.context!!, android.R.layout.simple_spinner_item, choices).also {
                        it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
        })
    }

    // DatePicker date set callback
    private val onDateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
        // MonthOfYear uses values between 1 and 12, whereas DatePicker uses 0-11
        editorViewModel.setDate(LocalDate.of(year, month+1, dayOfMonth))
    }

    private fun openDatePicker() {
        // Open DatePicker with the date
        editorViewModel.date.value!!.let { date ->
            val picker = DatePickerDialog(this.activity!!, R.style.DatePickerDialog, onDateSetListener, date.year, date.monthValue-1, date.dayOfMonth)
            picker.show()
        }
    }

    private fun setDateViewText(date: LocalDate) {
        this.date_view.text = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun saveTransaction() {
        when (arguments?.getInt(ARG_ACTION)) {
            ACTION_CREATE -> {
                val categoryId = this.category_spinner.selectedItemPosition.let {
                    // The first choice represents 'no category'
                    // Since the categories list begins at 0, we must subtract 1 from the position
                    if (it == 0) null else editorViewModel.categories.value!![it-1].id
                }
                editorViewModel.createTransaction(
                        BigDecimal(this.amount_input.text.toString().let { if (it.isEmpty()) "0" else it }),
                        Currency.getInstance(this.currency_spinner.selectedItem.toString()),
                        categoryId,
                        this.description_input.text.toString(),
                        this.supplier_input.text.toString(),
                        this.document_type_input.text.toString(),
                        this.document_number_input.text.toString()
                )
            }
        }
    }

    private fun finishEditing(transaction: Transaction) {
        this.onEditFinishedListener.onTransactionEditFinished(transaction)
    }

    /** Interface for transaction edit callback. */
    interface OnTransactionEditFinishedListener {
        fun onTransactionEditFinished(transaction: Transaction)
    }

    companion object {
        const val TAG = "TRANSACTION_EDITOR_FRAGMENT"

        const val ARG_ACTION = "ACTION"

        const val ACTION_CREATE = 0
        const val ACTION_EDIT = 1

        private const val DATE_PICKER_TAG = "DATEPICKER"
    }
}
