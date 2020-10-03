package ro.ande.dekont.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_transaction_detail.*
import ro.ande.dekont.R
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.util.setupWithIndividualNavController
import ro.ande.dekont.viewmodel.TransactionDetailViewModel
import ro.ande.dekont.viewmodel.injectableViewModel
import ro.ande.dekont.vo.Transaction

/**
 * A [Fragment] for viewing the details of a [Transaction].
 *
 */
class TransactionDetailFragment : Fragment(), Injectable {
    private val transactionDetailViewModel: TransactionDetailViewModel by injectableViewModel()

    private val navArgs: TransactionDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        transaction_detail_toolbar.setupWithIndividualNavController(findNavController())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Observe transaction
        transactionDetailViewModel.transaction.observe(viewLifecycleOwner) { transaction ->
            showTransactionDetails(transaction)
        }

        // Load transaction on launch
        if (transactionDetailViewModel.transaction.value == null) {
            val id = navArgs.transactionId
            transactionDetailViewModel.loadTransaction(id)
        }
    }

    /** Displays the transaction details in the layout. */
    private fun showTransactionDetails(transaction: Transaction) {
        this.date_view.text = transaction.formattedDate
        this.amount_view.text = transaction.formattedAmount
        this.currency_view.text = transaction.currency.currencyCode
        this.description_view.text = transaction.description.let {
            if (it.isEmpty())
                getString(R.string.message_description_unavailable)
            else it
        }
        this.supplier_view.text = transaction.supplier
        this.document_type_view.text = transaction.documentType
        this.document_number_view.text = transaction.documentNumber
    }
}
