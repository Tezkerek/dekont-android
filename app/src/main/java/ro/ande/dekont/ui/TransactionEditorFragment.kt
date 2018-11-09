package ro.ande.dekont.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import ro.ande.dekont.vo.Transaction
import ro.ande.dekont.R
import java.util.*
import kotlinx.android.synthetic.main.fragment_transaction_editor.*
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

/**
 * A [Fragment] for editing a [Transaction].
 *
 */
class TransactionEditorFragment : Fragment() {
    private lateinit var onEditFinishedListener: OnTransactionEditFinishedListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_editor, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        onEditFinishedListener = this.activity as OnTransactionEditFinishedListener

        setDateViewText(LocalDate.now())
        populateCurrencySpinner()
    }

    // Populates the spinner with currencies
    private fun populateCurrencySpinner() {
        val currencies = Currency.getAvailableCurrencies().map { it.currencyCode }.sorted()

        val adapter = ArrayAdapter<String>(this.context!!, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        this.currency_spinner.adapter = adapter
    }

    private fun setDateViewText(date: LocalDate) {
        this.date_view.text = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    /** Interface for transaction edit callback. */
    interface OnTransactionEditFinishedListener {
        fun onTransactionEditFinished(transaction: Transaction)
    }

    companion object {
        const val ARG_ACTION = "ACTION"

        const val ACTION_CREATE = 0
        const val ACTION_EDIT = 1
    }
}
