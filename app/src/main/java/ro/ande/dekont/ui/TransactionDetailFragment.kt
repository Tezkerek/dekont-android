package ro.ande.dekont.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_transaction_detail.*
import ro.ande.dekont.R
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.viewmodel.TransactionDetailViewModel
import ro.ande.dekont.vo.Transaction
import javax.inject.Inject

/**
 * A [Fragment] for viewing the details of a [Transaction].
 *
 */
class TransactionDetailFragment : Fragment(), Injectable {
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var transactionDetailViewModel: TransactionDetailViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        transactionDetailViewModel = ViewModelProviders.of(this, mViewModelFactory).get(TransactionDetailViewModel::class.java)

        // Observe transaction
        transactionDetailViewModel.transaction.observe(this, Observer { transaction -> showTransactionDetails(transaction) })

        // Load transaction on launch
        if (transactionDetailViewModel.transaction.value == null) {
            val id = arguments!!.getInt(ARG_TRANSACTION_ID)
            transactionDetailViewModel.loadTransaction(id)
        }
    }

    /** Displays the transaction details in the layout. */
    private fun showTransactionDetails(transaction: Transaction) {
        this.date_view.text = transaction.formattedDate
        this.amount_view.text = transaction.formattedAmount
        this.currency_view.text = transaction.currency.currencyCode
        this.description_view.text = transaction.description
        this.supplier_view.text = transaction.supplier
        this.document_type_view.text = transaction.documentType
        this.document_number_view.text = transaction.documentNumber
    }

    companion object {
        const val ARG_TRANSACTION_ID = "TRANSACTION_ID"
    }
}
