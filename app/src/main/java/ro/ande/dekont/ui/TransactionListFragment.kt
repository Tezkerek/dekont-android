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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_transaction_list.*
import org.zakariya.stickyheaders.PagedLoadScrollListener
import org.zakariya.stickyheaders.StickyHeaderLayoutManager
import ro.ande.dekont.R
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.util.NetworkState
import ro.ande.dekont.viewmodel.TransactionListViewModel
import javax.inject.Inject

class TransactionListFragment : Fragment(), Injectable {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var transactionListViewModel: TransactionListViewModel

    private var transactionsLoadCompleteNotifier: PagedLoadScrollListener.LoadCompleteNotifier? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_transaction_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            transactionListViewModel = ViewModelProviders.of(it, mViewModelFactory).get(TransactionListViewModel::class.java)
        }

        initTransactionList()

        // Callback for add transaction FAB
        this.add_transaction_fab.setOnClickListener { navigateToNewTransactionEditor() }

        // Observe transaction list
        transactionListViewModel.transactionsWithCategories.observe(viewLifecycleOwner, Observer { pair ->
            val transactions = pair.first
            val categoriesResource = pair.second

            when {
                categoriesResource.isError() -> showResourceError(categoriesResource.message, ResourceType.CATEGORY_LIST)
            }

            // Set transactions and categories in adapter
            this.transaction_list.adapter.also { adapter ->
                adapter as TransactionRecyclerViewAdapter

                categoriesResource.data?.let { adapter.setCategories(it) }
                adapter.mergeTransactions(transactions.getAll())
            }
        })

        // Observe transactions network state
        transactionListViewModel.transactionsState.observe(viewLifecycleOwner, Observer { state: NetworkState? ->
            // Sometimes state is null after login, no idea why
            if (state != null) {
                // On success or error, notify load complete
                if (state.state != NetworkState.Status.LOADING) {
                    transactionsLoadCompleteNotifier?.notifyLoadComplete()
                    transactionListViewModel.transactionsLastLoadedPage++
                }

                if (state.state == NetworkState.Status.ERROR) showResourceError(state.message, ResourceType.TRANSACTION_LIST)

                // Stop loading if data has been exhausted
                if (state.isExhausted) transactionsLoadCompleteNotifier?.notifyLoadExhausted()
            }
        })

        // Load transactions on launch
        if (transactionListViewModel.transactions.value == null) {
            transactionListViewModel.loadTransactionsWithCategories(1)
        }
    }

    /**
     * Initialise the transaction list.
     */
    private fun initTransactionList() {
        this.transaction_list.apply {
            val stickyHeaderLayoutManager = StickyHeaderLayoutManager()

            layoutManager = stickyHeaderLayoutManager
            adapter = TransactionRecyclerViewAdapter()
                    .also {
                        it.setOnTransactionClickListener { id -> onTransactionClick(id) }
                        it.setOnTransactionLongPressListener { id -> openTransactionOptionsMenu(id)}
                    }

            addOnScrollListener(object : PagedLoadScrollListener(stickyHeaderLayoutManager, 2) {
                override fun onLoadMore(page: Int, loadComplete: LoadCompleteNotifier) {
                    transactionsLoadCompleteNotifier = loadComplete

                    // Skip the first page, we will load it with categories later
                    if (page == 1) {
                        loadComplete.notifyLoadComplete()
                        return
                    }

                    // Compare with ViewModel state
                    // Check if data is exhausted
                    if (transactionListViewModel.transactionsState.value?.isExhausted == true) {
                        loadComplete.notifyLoadExhausted()
                        return
                    }

                    // Skip pages that we already loaded
                    if (page <= transactionListViewModel.transactionsLastLoadedPage)
                        return

                    transactionListViewModel.loadTransactions(page)
                }
            })

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy < 0) {
                        // Show FAB on scroll up
                        this@TransactionListFragment.add_transaction_fab.show()
                    } else if (dy > 0) {
                        // Hide FAB on scroll down
                        this@TransactionListFragment.add_transaction_fab.hide()
                    }
                }
            })
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
                                    .setPositiveButton(R.string.action_confirm) { confirmationDialog, _ ->
                                        // TODO Progress indicator (maybe on toolbar)
                                        transactionListViewModel.deleteTransaction(transactionId).observe(viewLifecycleOwner, Observer { deletion ->
                                            if (deletion.isSuccess()) {
                                                // Remove item manually from list
                                                this.transaction_list.adapter.let { adapter ->
                                                    adapter as TransactionRecyclerViewAdapter
                                                    adapter.removeTransaction(transactionId)
                                                }
                                                Snackbar.make(this.view!!, R.string.message_transaction_deletion_success, Snackbar.LENGTH_LONG).show()
                                            } else {
                                                Snackbar.make(this.view!!, deletion.message ?: getString(R.string.error_unknown), Snackbar.LENGTH_LONG).show()
                                            }
                                        })
                                        confirmationDialog.dismiss()
                                    }
                                    .setNegativeButton(R.string.action_cancel) { confirmationDialog, _ ->
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

    private fun showResourceError(message: String?, type: ResourceType) {
        val prefix = when (type) {
            ResourceType.TRANSACTION_LIST -> R.string.error_loading_transactions_prefix
            ResourceType.CATEGORY_LIST -> R.string.error_loading_categories_prefix
        }
        val fullMessage = getString(prefix, message ?: getString(R.string.error_unknown))

        Snackbar.make(this.transaction_list, fullMessage, Snackbar.LENGTH_INDEFINITE).show()
    }

    private fun onTransactionClick(id: Int) {
        // Navigate to TransactionDetail
        findNavController().navigate(TransactionListFragmentDirections.actionTransactionListFragmentToTransactionDetailFragment(id))
    }

    private fun navigateToNewTransactionEditor() {
        findNavController().navigate(TransactionListFragmentDirections.actionTransactionListFragmentToTransactionEditorFragment(TransactionEditorFragment.Action.CREATE))
    }

    companion object {
        const val TAG = "TRANSACTION_LIST_FRAGMENT"

        private enum class ResourceType {
            TRANSACTION_LIST, CATEGORY_LIST
        }
    }
}
