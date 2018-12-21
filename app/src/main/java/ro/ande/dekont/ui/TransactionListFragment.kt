package ro.ande.dekont.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_transaction_list.*
import ro.ande.dekont.R
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.util.StickyHeaderLayoutManager
import ro.ande.dekont.viewmodel.TransactionsViewModel
import javax.inject.Inject

class TransactionListFragment : Fragment(), Injectable {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var transactionsViewModel: TransactionsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            view.layoutManager = StickyHeaderLayoutManager()
            view.adapter = TransactionRecyclerViewAdapter()
                    .also { it.setOnTransactionLongPressListener { id -> openTransactionOptionsMenu(id)} }
        }


        // Long press transaction listener


        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            transactionsViewModel = ViewModelProviders.of(it, mViewModelFactory).get(TransactionsViewModel::class.java)
        }

        // Observe transaction list
        transactionsViewModel.transactions.observe(this, Observer { transactionsResource ->
            val transactions = transactionsResource.data
            if (transactions != null) {
                this@TransactionListFragment.transaction_list.adapter.run {
                    this as TransactionRecyclerViewAdapter
                    this.setTransactions(transactions)
                }
            }
        })
        transactionsViewModel.loadTransactions()

        transactionsViewModel.snackbarMessage.observe(this, Observer { message ->
            if (message != null) {
                this@TransactionListFragment.transaction_list.run {
                    Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()
                }
            }
        })
    }

    fun openTransactionOptionsMenu(transactionId: Int) {
        AlertDialog.Builder(this.activity)
                .setItems(R.array.transaction_options) { dialog, optionIndex ->
                    when (optionIndex) {
                        0 -> {
                            // Show delete confirmation dialog
                            AlertDialog.Builder(this.activity)
                                    .setMessage(R.string.dialog_message_confirm_transaction_deletion)
                                    .setPositiveButton(R.string.dialog_action_confirm) { confirmationDialog, _ ->
                                        // TODO Progress indicator (maybe on toolbar)
                                        transactionsViewModel.deleteTransaction(transactionId).observe(this, Observer { deletion ->
                                            if (deletion.isSuccess()) {
                                                Snackbar.make(this.view!!, R.string.message_transaction_deletion_success, Snackbar.LENGTH_LONG).show()
                                            } else {
                                                Snackbar.make(this.view!!, deletion.message ?: getString(R.string.error_unknown), Snackbar.LENGTH_LONG).show()
                                            }
                                        })
                                        confirmationDialog.dismiss()
                                    }
                                    .setNegativeButton(R.string.dialog_action_cancel) { confirmationDialog, _ ->
                                        confirmationDialog.dismiss()
                                    }
                                    .create()
                                    .show()
                        }
                    }
                }
                .create()
                .show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    companion object {
        const val TAG = "TRANSACTION_LIST_FRAGMENT"
    }
}
