package ro.ande.dekont.ui

import android.app.AlertDialog
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
import org.zakariya.stickyheaders.PagedLoadScrollListener
import org.zakariya.stickyheaders.StickyHeaderLayoutManager
import ro.ande.dekont.R
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.viewmodel.TransactionListViewModel
import ro.ande.dekont.vo.Resource
import javax.inject.Inject

class TransactionListFragment : Fragment(), Injectable {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var transactionListViewModel: TransactionListViewModel

    private var transactionsLoadCompleteNotifier: PagedLoadScrollListener.LoadCompleteNotifier? = null

    private var onTransactionClickListener: OnTransactionClickListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val stickyHeaderLayoutManager = StickyHeaderLayoutManager()
            view.layoutManager = stickyHeaderLayoutManager
            view.adapter = TransactionRecyclerViewAdapter()
                    .also {
                        it.setOnTransactionClickListener { id -> onTransactionClickListener?.onTransactionClick(id) }
                        it.setOnTransactionLongPressListener { id -> openTransactionOptionsMenu(id)}
                    }

            view.addOnScrollListener(object : PagedLoadScrollListener(stickyHeaderLayoutManager) {
                override fun onLoadMore(page: Int, loadComplete: LoadCompleteNotifier) {
                    transactionsLoadCompleteNotifier = loadComplete
                    transactionListViewModel.loadTransactions(page)
                }

            })
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            transactionListViewModel = ViewModelProviders.of(it, mViewModelFactory).get(TransactionListViewModel::class.java)

            // Set transaction click listener
            onTransactionClickListener = it as OnTransactionClickListener
        }

        // Observe transaction list
        transactionListViewModel.transactionsWithCategories.observe(this, Observer { pair ->
            val transactionsResource = pair.first
            val categoriesResource = pair.second

            when {
                transactionsResource.isError() -> showResourceError(transactionsResource, ResourceType.TRANSACTION_LIST)
                categoriesResource.isError() -> showResourceError(categoriesResource, ResourceType.CATEGORY_LIST)
            }

            // Set transactions and categories in adapter
            this.transaction_list.adapter.also { adapter ->
                adapter as TransactionRecyclerViewAdapter
                transactionsResource.data?.let {
                    if (!transactionsResource.isLoading()) {
                        if (it.isEmpty()) {
                            transactionsLoadCompleteNotifier?.notifyLoadExhausted()
                        } else {
                            transactionsLoadCompleteNotifier?.notifyLoadComplete()
                            adapter.mergeTransactions(it)
                        }
                    }
                }
                categoriesResource.data?.let { adapter.setCategories(it) }
            }
        })

        // Load transactions on launch
        if (transactionListViewModel.transactions.value == null) {
            transactionListViewModel.loadTransactionsWithCategories(0)
        }
    }

    private fun openTransactionOptionsMenu(transactionId: Int) {
        AlertDialog.Builder(this.activity)
                .setItems(R.array.transaction_options) { _, optionIndex ->
                    when (optionIndex) {
                        0 -> {
                            // Show delete confirmation dialog
                            AlertDialog.Builder(this.activity)
                                    .setMessage(R.string.dialog_message_confirm_transaction_deletion)
                                    .setPositiveButton(R.string.dialog_action_confirm) { confirmationDialog, _ ->
                                        // TODO Progress indicator (maybe on toolbar)
                                        transactionListViewModel.deleteTransaction(transactionId).observe(this, Observer { deletion ->
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

    private fun showResourceError(resource: Resource<*>, type: ResourceType) {
        val prefix = when (type) {
            ResourceType.TRANSACTION_LIST -> R.string.error_loading_transactions_prefix
            ResourceType.CATEGORY_LIST -> R.string.error_loading_categories_prefix
        }
        val message = getString(prefix, resource.message ?: getString(R.string.error_unknown))

        Snackbar.make(this.transaction_list, message, Snackbar.LENGTH_INDEFINITE).show()
    }

    interface OnTransactionClickListener {
        fun onTransactionClick(id: Int)
    }

    companion object {
        const val TAG = "TRANSACTION_LIST_FRAGMENT"

        private enum class ResourceType {
            TRANSACTION_LIST, CATEGORY_LIST
        }
    }
}
