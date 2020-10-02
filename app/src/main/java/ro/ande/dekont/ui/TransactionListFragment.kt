package ro.ande.dekont.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_transaction_list.*
import kotlinx.coroutines.launch
import ro.ande.dekont.R
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.util.NetworkState
import ro.ande.dekont.util.PagedLoadScrollListener
import ro.ande.dekont.viewmodel.TransactionListViewModel
import ro.ande.dekont.viewmodel.injectableViewModel

class TransactionListFragment : Fragment(), Injectable {
    private val transactionListViewModel: TransactionListViewModel by injectableViewModel()

    private var transactionsLoadCompleteNotifier: PagedLoadScrollListener.LoadCompleteNotifier? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_transaction_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initTransactionList()

        // Callback for add transaction FAB
        this.add_transaction_fab.setOnClickListener { navigateToNewTransactionEditor() }

        // Observe transaction list
        transactionListViewModel.transactionsWithCategories.observe(viewLifecycleOwner) { pair ->
            val transactions = pair.first
            val categoriesResource = pair.second

            when {
                // TODO Make this error less obtrusive
                // categoriesResource.isError() -> showResourceError(categoriesResource.message, ResourceType.CATEGORY_LIST)
            }

            // Set transactions and categories in adapter
            lifecycleScope.launch {
                transaction_list.adapter.also { adapter ->
                    adapter as ITransactionListManager

                    categoriesResource.data?.let { adapter.setCategories(it) }
                    adapter.setTransactions(transactions.getAll())
                }
            }
        }

        // Observe transactions network state
        transactionListViewModel.transactionsState.observe(viewLifecycleOwner) { state: NetworkState? ->
            // Sometimes state is null after login, no idea why
            if (state == null) return@observe

            // On success or error, notify load complete
            if (state.state != NetworkState.Status.LOADING) {
                transactionsLoadCompleteNotifier?.notifyLoadComplete()
                transactionListViewModel.transactionsLastLoadedPage++
            }

            if (state.state == NetworkState.Status.ERROR) showResourceError(state.message, ResourceType.TRANSACTION_LIST)

            // Stop loading if data has been exhausted
            if (state.isExhausted) transactionsLoadCompleteNotifier?.notifyLoadExhausted()
        }

        // Load transactions on launch
        if (transactionListViewModel.transactions.value == null) {
            transactionListViewModel.loadTransactionsWithCategories(1)
        }
    }

    /**
     * Initialise the transaction list.
     */
    private fun initTransactionList() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        val transactionListAdapter = TransactionListAdapter()
                .apply {
                    onTransactionClickListener = ITransactionListManager.OnTransactionClickListener { id -> handleTransactionClick(id) }
                    onTransactionLongClickListener = ITransactionListManager.OnTransactionLongClickListener { id -> openTransactionOptionsMenu(id) }
                }

        this.transaction_list.apply {
            layoutManager = linearLayoutManager

            val headerItemDecoration = HeaderItemDecoration(this, isHeader = transactionListAdapter::isItemHeader)
            addItemDecoration(headerItemDecoration)

            adapter = transactionListAdapter

            addOnScrollListener(object : PagedLoadScrollListener(linearLayoutManager, 2) {
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
                    this@TransactionListFragment.add_transaction_fab.run {
                        // Toggle FAB on scroll
                        if (dy < 0)
                            show()
                        else if (dy > 0)
                            hide()
                    }
                }
            })
        }
    }

    private fun openTransactionOptionsMenu(transactionId: Int) {
        AlertDialog.Builder(this.context)
                .setItems(R.array.transaction_options) { _, optionIndex ->
                    when (optionIndex) {
                        0 -> confirmTransactionDeletion(transactionId)
                    }
                }
                .create()
                .show()
    }

    /**
     * Show a confirmation dialog and delete the transaction if answer is positive.
     */
    private fun confirmTransactionDeletion(transactionId: Int) {
        // Show delete confirmation dialog
        AlertDialog.Builder(this.context)
                .setMessage(R.string.dialog_message_confirm_transaction_deletion)
                .setPositiveButton(R.string.action_confirm) { confirmationDialog, _ ->
                    // TODO Progress indicator (maybe on toolbar)
                    transactionListViewModel.deleteTransaction(transactionId).observe(viewLifecycleOwner) { deletion ->
                        if (deletion.isSuccess()) {
                            // Remove item manually from list
//                            this.transaction_list.adapter.let { adapter ->
//                                adapter as TransactionRecyclerViewAdapter
//                                adapter.removeTransaction(transactionId)
//                            }
                            Snackbar.make(this.requireView(), R.string.message_transaction_deletion_success, Snackbar.LENGTH_LONG).show()
                        } else {
                            Snackbar.make(this.requireView(), deletion.message
                                    ?: getString(R.string.error_unknown), Snackbar.LENGTH_LONG).show()
                        }
                    }
                    confirmationDialog.dismiss()
                }
                .setNegativeButton(R.string.action_cancel) { confirmationDialog, _ ->
                    confirmationDialog.dismiss()
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

    private fun handleTransactionClick(id: Int) {
        // Navigate to TransactionDetail
        findNavController().navigate(TransactionListFragmentDirections.actionTransactionListFragmentToTransactionDetailFragment(id))
    }

    private fun navigateToNewTransactionEditor() {
        findNavController().navigate(TransactionListFragmentDirections.actionTransactionListFragmentToTransactionEditorFragment(TransactionEditorFragment.Action.CREATE))
    }

    companion object {
        private enum class ResourceType {
            TRANSACTION_LIST, CATEGORY_LIST
        }
    }
}
