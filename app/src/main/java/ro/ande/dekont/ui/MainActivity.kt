package ro.ande.dekont.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import ro.ande.dekont.BaseActivity
import ro.ande.dekont.R
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.viewmodel.MainViewModel
import ro.ande.dekont.viewmodel.injectableViewModel

class MainActivity : BaseActivity(), Injectable, AuthSessionManager {
    private val mainViewModel: MainViewModel by injectableViewModel()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // https://stackoverflow.com/a/62612502/4904553
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController = navHostFragment.navController

        setupLoginCheck()

        // Post-login actions
        if (this.intent.getBooleanExtra(INTENT_ARG_IS_POST_LOGIN, false)) executePostLogin()
    }

    private fun setupLoginCheck() {
        mainViewModel.isLoginValid.observe(this) { isLoggedIn ->
            if (!isLoggedIn!!) {
                redirectToLogin()
            }
        }
        mainViewModel.verifyLogin()
    }

    private fun executePostLogin() {
        // TODO Open group settings here, if user is not already in a group
    }

    private fun redirectToLogin() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun performLogout() {
        AlertDialog.Builder(this)
            .setMessage(R.string.dialog_message_confirm_logout)
            .setPositiveButton(R.string.action_sign_out) { dialog, _ ->
                mainViewModel.logout()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_cancel) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    companion object {
        const val INTENT_ARG_IS_POST_LOGIN = "IS_POST_LOGIN"
    }
}

interface AuthSessionManager {
    fun performLogout()
}