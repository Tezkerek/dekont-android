package ro.ande.dekont.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_transaction_list.*
import ro.ande.dekont.R
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.util.NetworkState
import ro.ande.dekont.util.PagedLoadScrollListener
import ro.ande.dekont.util.StringResource
import ro.ande.dekont.util.setupWithIndividualNavController
import ro.ande.dekont.viewmodel.TransactionListViewModel
import ro.ande.dekont.viewmodel.injectableViewModel

class TransactionListFragment : Fragment(), Injectable {
    private val transactionListViewModel: TransactionListViewModel by injectableViewModel()

    private var transactionListManager: TransactionListManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_transaction_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupNavigationUI(findNavController())

        add_transaction_fab.setOnClickListener { navigateToNewTransactionEditor() }
    }

    private fun setupNavigationUI(navController: NavController) {
        // Setup toolbar and drawer
        transaction_list_bottom_app_bar.setupWithIndividualNavController(navController, drawer_layout)

        nav_drawer.also { drawer ->
            drawer.setupWithNavController(navController)
            drawer.setNavigationItemSelectedListener { item ->
                drawer_layout.closeDrawer(drawer)

                when (item.itemId) {
                    R.id.nav_logout ->
                        (activity as? AuthSessionManager)?.performLogout()
                    R.id.groupSettingsFragment ->
                        navigateToGroupSettings()
                    else ->
                        return@setNavigationItemSelectedListener false
                }
                true
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initTransactionList()

        lifecycleScope.launchWhenCreated {
            for (message in transactionListViewModel.messages) {
                showBottomSnackbar(message)
            }
        }

        // Observe transaction list
        transactionListViewModel.transactionsWithCategories.observe(viewLifecycleOwner) { pair ->
            val transactions = pair.first
            val categoriesResource = pair.second

            when {
                // TODO Make this error less obtrusive
                // categoriesResource.isError() -> showResourceError(categoriesResource.message, ResourceType.CATEGORY_LIST)
            }

            // Set transactions and categories in adapter
            transaction_list.adapter.also { adapter ->
                adapter as ITransactionListManager

                categoriesResource.data?.let { adapter.setCategories(it) }
                adapter.setTransactions(transactions.getAll())
            }
        }

        // Observe transactions network state
        transactionListViewModel.transactionsState.observe(viewLifecycleOwner) { state: NetworkState? ->
            // Sometimes state is null after login, no idea why
            if (state == null) return@observe

            // On success or error, notify load complete
            if (!state.isLoading) {
                transactionListManager?.notifyLoadComplete()
                transactionListViewModel.transactionsLastLoadedPage++
            }

            if (state.isError) showResourceError(state.message, ResourceType.TRANSACTION_LIST)

            // Stop loading if data has been exhausted
            if (state.isExhausted) transactionListManager?.notifyLoadExhausted()
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
        transactionListManager = TransactionListManager(transaction_list).apply {
            onTransactionClickListener = { id -> handleTransactionClick(id) }
            onTransactionLongClickListener = { id -> openTransactionOptionsMenu(id) }
            onPageLoadListener = onPageLoad@{ page ->
                // Skip the first page, we will load it with categories later
                if (page == 1) {
                    notifyLoadComplete()
                    return@onPageLoad
                }

                // Compare with ViewModel state
                // Check if data is exhausted
                if (transactionListViewModel.transactionsState.value?.isExhausted == true) {
                    notifyLoadExhausted()
                    return@onPageLoad
                }

                // Skip pages that we already loaded
                if (page <= transactionListViewModel.transactionsLastLoadedPage)
                    return@onPageLoad

                transactionListViewModel.loadTransactions(page)
            }
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
                    transactionListViewModel.attemptTransactionDeletion(transactionId)
                    confirmationDialog.dismiss()
                }
                .setNegativeButton(R.string.action_cancel) { confirmationDialog, _ ->
                    confirmationDialog.dismiss()
                }
                .create()
                .show()
    }

    private fun showBottomSnackbar(text: CharSequence, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(coordinator_layout, text, duration)
                .setAnchorView(add_transaction_fab)
                .show()
    }

    private fun showBottomSnackbar(stringResource: StringResource, duration: Int = Snackbar.LENGTH_SHORT) =
            showBottomSnackbar(stringResource.getString(requireContext()), duration)

    private fun showBottomSnackbar(@StringRes textResId: Int, duration: Int = Snackbar.LENGTH_SHORT) =
            showBottomSnackbar(getString(textResId), duration)

    private fun showResourceError(message: String?, type: ResourceType) {
        val prefix = when (type) {
            ResourceType.TRANSACTION_LIST -> R.string.error_loading_transactions_prefix
            ResourceType.CATEGORY_LIST -> R.string.error_loading_categories_prefix
        }
        val fullMessage = getString(prefix, message ?: getString(R.string.error_unknown))

        showBottomSnackbar(fullMessage, Snackbar.LENGTH_LONG)
    }

    private fun handleTransactionClick(id: Int) {
        // Navigate to TransactionDetail
        findNavController().navigate(TransactionListFragmentDirections.actionTransactionListFragmentToTransactionDetailFragment(id))
    }

    private fun navigateToNewTransactionEditor() {
        findNavController().navigate(TransactionListFragmentDirections.actionTransactionListFragmentToTransactionEditorFragment(TransactionEditorFragment.Action.CREATE))
    }

    private fun navigateToGroupSettings() =
            findNavController().navigate(TransactionListFragmentDirections.actionTransactionListFragmentToGroupSettingsFragment())

    companion object {
        private enum class ResourceType {
            TRANSACTION_LIST, CATEGORY_LIST
        }
    }
}

class TransactionListManager(recyclerView: RecyclerView) {
    var onTransactionClickListener: (Int) -> Unit = {}
    var onTransactionLongClickListener: (Int) -> Unit = {}
    var onPageLoadListener: (Int) -> Unit = {}

    private var loadCompleteNotifier: PagedLoadScrollListener.LoadCompleteNotifier? = null

    init {
        val linearLayoutManager = LinearLayoutManager(recyclerView.context)
        val transactionListAdapter = TransactionListAdapter()
                .apply {
                    onTransactionClickListener = ITransactionListManager.OnTransactionClickListener(this@TransactionListManager.onTransactionClickListener)
                    onTransactionLongClickListener = ITransactionListManager.OnTransactionLongClickListener(this@TransactionListManager.onTransactionLongClickListener)
                }


        recyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = transactionListAdapter
            addItemDecoration(HeaderItemDecoration(this, isHeader = transactionListAdapter::isItemHeader))
            addOnScrollListener(object : PagedLoadScrollListener(linearLayoutManager, 2) {
                override fun onLoadMore(page: Int, loadComplete: LoadCompleteNotifier) {
                    loadCompleteNotifier = loadComplete
                    onPageLoadListener(page)
                }
            })
        }
    }

    fun notifyLoadComplete() = loadCompleteNotifier?.notifyLoadComplete()
    fun notifyLoadExhausted() = loadCompleteNotifier?.notifyLoadExhausted()
}
