package ro.ande.dekont

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_transactions.*

class TransactionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
        setSupportActionBar(toolbar)

        CheckLoginTask().execute()
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

    /**
     * Verifies login status and redirects the user to the login page if needed.
     */
    inner class CheckLoginTask : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void): Boolean {
            return DekontApi(this@TransactionsActivity).isLoginValid()
        }

        override fun onPostExecute(isLoggedIn: Boolean) {
            if (!isLoggedIn) {
                showLoginRedirectDialog()
            }
        }
    }
}
