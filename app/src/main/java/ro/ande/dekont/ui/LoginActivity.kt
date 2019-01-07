package ro.ande.dekont.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_login.*
import ro.ande.dekont.BaseActivity
import ro.ande.dekont.R
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.viewmodel.LoginViewModel
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.Token
import javax.inject.Inject

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : BaseActivity(), Injectable {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(LoginViewModel::class.java)

        mViewModel.authToken.observe(this, LoginObserver())

        // Set up the login form.
        login_password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        email_sign_in_button.setOnClickListener { attemptLogin() }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        // Reset errors.
        login_email.error = null
        login_password.error = null

        // Store values at the time of the login attempt.
        val emailStr = login_email.text.toString()
        val passwordStr = login_password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            login_password.error = getString(R.string.error_invalid_password)
            focusView = login_password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            login_email.error = getString(R.string.error_field_required)
            focusView = login_email
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            login_email.error = getString(R.string.error_invalid_email)
            focusView = login_email
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)

            // Generate a random number to identify device
            val deviceName = "android-" + Math.ceil(Math.random() * 100)

            mViewModel.attemptLogin(emailStr, passwordStr, deviceName)
        }
    }

    /**
     * Callback for the login attempt.
     */
    private inner class LoginObserver : Observer<Resource<Token>> {
        override fun onChanged(tokenResource: Resource<Token>?) {
            if (tokenResource!!.isSuccess()) {
                val token = tokenResource.data!!

                // Store the token
                // TODO: Figure out how to use AccountManager
                val sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                sharedPreferences.edit().putString(SHARED_PREFERENCES_TOKEN_KEY, token.token).apply()

                finishLogin()
            } else {
                // Display an error popup
//                Snackbar.make(this@LoginActivity.login_form, R.string.error_sign_in_failed, Snackbar.LENGTH_SHORT).show()
                Snackbar.make(this@LoginActivity.email_login_form, tokenResource.message!!, Snackbar.LENGTH_SHORT).show()
            }

            showProgress(false)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        email_sign_in_button.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        email_sign_in_button.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        progress_indicator.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        progress_indicator.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    private fun finishLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        const val SHARED_PREFERENCES_NAME = "auth"
        const val SHARED_PREFERENCES_TOKEN_KEY = "token"
    }
}
