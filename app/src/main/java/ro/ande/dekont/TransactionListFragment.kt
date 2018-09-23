package ro.ande.dekont

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_transaction_list.*
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.viewmodel.TransactionsViewModel
import javax.inject.Inject

class TransactionListFragment : Fragment(), Injectable {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var transactionsViewModel: TransactionsViewModel

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
//                adapter = TransactionRecyclerViewAdapter(DummyContent.ITEMS)
            }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.let {
            transactionsViewModel = ViewModelProviders.of(it, mViewModelFactory).get(TransactionsViewModel::class.java)
        }

        transactionsViewModel.loadTransactions().observe(this, Observer { transactions ->
            this@TransactionListFragment.transaction_list.run {
                adapter = TransactionRecyclerViewAdapter(transactions)
                if (transactions.isError()) {
//                    Snackbar.make(this, transactions.message ?: getString(R.string.error_unknown), Snackbar.LENGTH_LONG).show()
                }
            }
        })

        transactionsViewModel.snackbarMessage.observe(this, Observer { message ->
            if (message != null) {
                this@TransactionListFragment.transaction_list.run {
                    Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    companion object {
        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
                TransactionListFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }
}
