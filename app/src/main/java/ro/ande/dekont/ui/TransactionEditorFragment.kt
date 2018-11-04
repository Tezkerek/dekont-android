package ro.ande.dekont.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ro.ande.dekont.vo.Transaction
import ro.ande.dekont.R

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
