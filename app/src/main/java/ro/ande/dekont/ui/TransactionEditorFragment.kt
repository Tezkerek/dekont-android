package ro.ande.dekont.ui


import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_transaction_editor.*
import ro.ande.dekont.R
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.util.setupWithIndividualNavController
import ro.ande.dekont.viewmodel.TransactionEditorViewModel
import ro.ande.dekont.viewmodel.injectableViewModel
import ro.ande.dekont.vo.Transaction

/**
 * A [Fragment] for editing a [Transaction].
 *
 */
class TransactionEditorFragment : Fragment(), Injectable {
    private val editorViewModel: TransactionEditorViewModel by injectableViewModel()

    private val navArgs: TransactionEditorFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        transaction_editor_toolbar.setupWithIndividualNavController(findNavController())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Setup data
        populateCurrencySpinner()
        populateCategorySpinner()

        // Observe result transaction
        editorViewModel.transactionResource.observe(viewLifecycleOwner, Observer { transactionResource ->
            if (transactionResource.isSuccess()) {
                finishEditing()
            } else if (transactionResource.isError()) {
                Snackbar.make(this.requireView(), transactionResource.message ?: "", Snackbar.LENGTH_SHORT).show()
            }
        })

        transaction_editor_form.setOnTransactionSaveListener {
            closeKeyboard()
            saveTransaction(it)
        }
    }

    // Populates the spinner with currencies
    private fun populateCurrencySpinner() {
        editorViewModel.currencies.observe(viewLifecycleOwner) {
            transaction_editor_form.setCurrencies(it)
        }
    }

    private fun populateCategorySpinner() {
        editorViewModel.categories.observe(viewLifecycleOwner) { categories ->
            transaction_editor_form.setCategories(categories)
        }
    }

    private fun saveTransaction(transaction: Transaction) {
        when (navArgs.editorAction) {
            Action.CREATE -> {
                editorViewModel.createTransaction(transaction)
            }
            Action.EDIT -> {}
        }
    }

    private fun closeKeyboard() {
        activity?.let { activity ->
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE).let {
                it as InputMethodManager
                it.hideSoftInputFromWindow(activity.currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }

    private fun finishEditing() {
        findNavController().popBackStack()
    }

    enum class Action {
        CREATE, EDIT
    }
}
