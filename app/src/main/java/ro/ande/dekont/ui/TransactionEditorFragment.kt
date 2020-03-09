package ro.ande.dekont.ui


import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_transaction_editor.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import ro.ande.dekont.R
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.viewmodel.TransactionEditorViewModel
import ro.ande.dekont.viewmodel.injectableViewModel
import ro.ande.dekont.vo.Transaction
import java.math.BigDecimal
import java.util.*

/**
 * A [Fragment] for editing a [Transaction].
 *
 */
class TransactionEditorFragment : Fragment(), Injectable {
    private val editorViewModel: TransactionEditorViewModel by injectableViewModel()

    private val navArgs: TransactionEditorFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_editor, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Fake handle a touch event to prevent propagation to the fragment below
        // We use this instead of making the view clickable because the latter makes
        // the EditText views flash white when we click the view.
        this.view?.setOnTouchListener { _, _ -> true }

        // Observe date and update the date_view
        editorViewModel.date.observe(viewLifecycleOwner, Observer { date -> setDateViewText(date) })

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
        editorViewModel.transactionResource.observe(viewLifecycleOwner, Observer { transactionResource ->
            if (transactionResource.isSuccess()) {
                finishEditing()
            } else if (transactionResource.isError()) {
                Snackbar.make(this.main_inputs_layout, transactionResource.message ?: "", Snackbar.LENGTH_SHORT).show()
            }
        })

        this.save_button.setOnClickListener {
            closeKeyboard()
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
        editorViewModel.categories.observe(viewLifecycleOwner, Observer { categories ->
            val choices = listOf("No category") + categories.map { it.name }
            this.category_spinner.adapter =
                    ArrayAdapter<String>(this.context!!, android.R.layout.simple_spinner_item, choices).also {
                        it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
        })
    }

    // DatePicker date set callback
    private val onDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
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
        when (navArgs.editorAction) {
            Action.CREATE -> {
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
            Action.EDIT -> {}
        }
    }

    private fun closeKeyboard() {
        activity?.let { activity ->
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE).let {
                it as InputMethodManager
                it.hideSoftInputFromWindow(activity.currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }

    private fun finishEditing() {
        findNavController().popBackStack()
    }

    enum class Action {
        CREATE, EDIT
    }

    companion object {
        const val TAG = "TRANSACTION_EDITOR_FRAGMENT"
    }
}
