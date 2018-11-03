package ro.ande.dekont.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_transactions.*
import ro.ande.dekont.BaseActivity
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.viewmodel.TransactionsViewModel
import javax.inject.Inject

class TransactionsActivity : BaseActivity(), Injectable {
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
        setSupportActionBar(toolbar)

        val transactionsViewModel = ViewModelProviders.of(this, mViewModelFactory).get(TransactionsViewModel::class.java)
        transactionsViewModel.isLoginValid.observe(this, Observer<Boolean> { isLoggedIn ->
            if (!isLoggedIn!!) {
                redirectToLogin()
            } else {
                Toast.makeText(this, "You are logged in", Toast.LENGTH_SHORT).show()
            }
        })

        transactionsViewModel.verifyLogin()

        this.add_transaction_fab.setOnClickListener { openNewTransactionEditor() }
    }

    private fun openNewTransactionEditor() {
        val fragment = TransactionEditorFragment()
        val args = Bundle()
        args.putInt(TransactionEditorFragment.ARG_ACTION, TransactionEditorFragment.ACTION_CREATE)
        fragment.arguments = args

        val transaction = supportFragmentManager.beginTransaction()

        transaction.add(fragment)
    }

    /**
     * Creates a dialog that asks the user whether they would like to be redirected to the login screen.
     */
    private fun showLoginRedirectDialog() {
        val buttonListener = DialogInterface.OnClickListener { dialog, button ->
            when (button) {
                DialogInterface.BUTTON_POSITIVE -> redirectToLogin()
            }
        }

        AlertDialog.Builder(this@TransactionsActivity)
                .setMessage(R.string.dialog_message_login_redirect)
                .setPositiveButton(R.string.dialog_positive_login_redirect, buttonListener)
                .setNegativeButton(R.string.dialog_negative_login_redirect, buttonListener)
                .show()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
