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
import ro.ande.dekont.api.ApiErrorResponse
import ro.ande.dekont.api.ApiErrors
import ro.ande.dekont.api.ApiSuccessResponse
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.viewmodel.AuthViewModel
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.Token
import javax.inject.Inject

/**
 * A login screen that offers login via email/password.
 */
class AuthActivity : BaseActivity(), Injectable {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        authViewModel = ViewModelProviders.of(this, mViewModelFactory).get(AuthViewModel::class.java)

        authViewModel.authToken.observe(this, LoginObserver())

        authViewModel.registrationResponse.observe(this, Observer { response ->
            when (response) {
                is ApiSuccessResponse -> {
                    // Show message and attempt login
                    Snackbar.make(this.registration_form, R.string.message_registration_successful, Snackbar.LENGTH_LONG).show()
                    finishRegistration()
                }
                is ApiErrorResponse -> showRegistrationErrors(response.errors)
            }
            showRegistrationProgress(false)
        })

        // Set up the login form.
        login_password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        email_sign_in_button.setOnClickListener { attemptLogin() }
        register_button.setOnClickListener { attemptRegistration() }
    }

    /**
     * Attempts to sign in the account specified by the login form.
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
            showLoginProgress(true)

            authViewModel.attemptLogin(emailStr, passwordStr)
        }
    }

    private fun attemptRegistration() {
        // Reset errors.
        registration_email.error = null
        registration_password.error = null

        // Store values at the time of the login attempt.
        val emailStr = registration_email.text.toString()
        val passwordStr = registration_password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            registration_password.error = getString(R.string.error_invalid_password)
            focusView = registration_password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            registration_email.error = getString(R.string.error_field_required)
            focusView = registration_email
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            registration_email.error = getString(R.string.error_invalid_email)
            focusView = registration_email
            cancel = true
        }

        if (cancel) {
            // Focus the field with the error
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and attempt registration.
            showRegistrationProgress(true)

            authViewModel.attemptRegistration(emailStr, passwordStr)
        }
    }

    private fun showRegistrationErrors(errors: ApiErrors) {
        errors.detail?.let { Snackbar.make(this.registration_form, it, Snackbar.LENGTH_LONG).show() }
        errors.nonFieldErrors.takeIf { it.isNotEmpty() }?.let { Snackbar.make(this.registration_form, it.first(), Snackbar.LENGTH_LONG).show() }
        errors.fieldErrors.also { fieldErrors ->
            fieldErrors["email"]?.let { this.registration_email.error = it.first() }
            fieldErrors["password"]?.let { this.registration_password.error = it.first() }
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
                Snackbar.make(this@AuthActivity.email_login_form, tokenResource.message!!, Snackbar.LENGTH_SHORT).show()
            }

            showLoginProgress(false)
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

    private fun showLoginProgress(show: Boolean) = showProgress(show, this.email_sign_in_button)

    private fun showRegistrationProgress(show: Boolean) = showProgress(show, this.register_button)

    /**
     * Shows the progress indicator and hides [viewToHide].
     */
    private fun showProgress(show: Boolean, viewToHide: View) {
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        viewToHide.animate()
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
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.INTENT_ARG_IS_POST_LOGIN, true)
        }
        startActivity(intent)
        finish()
    }

    private fun finishRegistration() {
        // Attempt login
        authViewModel.attemptLogin(this.registration_email.text.toString(), this.registration_password.text.toString())
        showLoginProgress(true)
    }

    companion object {
        const val SHARED_PREFERENCES_NAME = "auth"
        const val SHARED_PREFERENCES_TOKEN_KEY = "token"
    }
}
