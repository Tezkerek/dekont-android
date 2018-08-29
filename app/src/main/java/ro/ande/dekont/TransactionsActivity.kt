package ro.ande.dekont

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_transactions.*
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.viewmodel.TransactionsViewModel
import javax.inject.Inject

class TransactionsActivity : BaseActivity(), Injectable {
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
        setSupportActionBar(toolbar)

        val mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(TransactionsViewModel::class.java)
        mViewModel.isLoginValid.observe(this, Observer<Boolean> { isLoggedIn ->
            if (!isLoggedIn!!) {
                redirectToLogin()
            } else {
                Toast.makeText(this, "You are logged in", Toast.LENGTH_SHORT).show()
            }
        })

        mViewModel.verifyLogin()
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
