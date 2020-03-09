package ro.ande.dekont.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_transaction_list.*
import ro.ande.dekont.BaseActivity
import ro.ande.dekont.R
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.viewmodel.MainViewModel
import ro.ande.dekont.viewmodel.injectableViewModel

class MainActivity : BaseActivity(), Injectable {
    private val mainViewModel: MainViewModel by injectableViewModel()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(this.toolbar)

        navController = findNavController(R.id.main_nav_host)

        /* UI Setup */
        setupNavigationUI()

        setupLoginCheck()

        // Post-login actions
        if (this.intent.getBooleanExtra(INTENT_ARG_IS_POST_LOGIN, false)) executePostLogin()
    }

    private fun setupNavigationUI() {
        // Setup toolbar and drawer
        val appBarConfiguration = AppBarConfiguration(navController.graph, drawer_layout)
        nav_drawer.setupWithNavController(navController)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        this.nav_drawer.setNavigationItemSelectedListener { item ->
            item.isChecked = true
            this.drawer_layout.closeDrawers()

            when (item.itemId) {
                R.id.nav_logout -> performLogout()
            }

            item.onNavDestinationSelected(navController)
        }
    }

    private fun setupLoginCheck() {
        mainViewModel.isLoginValid.observe(this, Observer<Boolean> { isLoggedIn ->
            if (!isLoggedIn!!) {
                redirectToLogin()
            }
        })
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

    private fun performLogout() {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Open drawer when pressing the menu button
                this.drawer_layout.openDrawer(GravityCompat.START)
            }
        }
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(STATE_ARG_SHOW_ADD_FAB, this.add_transaction_fab.isOrWillBeShown)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        // Show add_fab
        if (savedInstanceState.getBoolean(STATE_ARG_SHOW_ADD_FAB)) {
            this.add_transaction_fab.show()
        } else {
            this.add_transaction_fab.hide()
        }
    }

    companion object {
        const val STATE_ARG_SHOW_ADD_FAB = "SHOW_ADD_FAB"
        const val INTENT_ARG_START_SCREEN = "START_SCREEN"
        const val INTENT_ARG_IS_POST_LOGIN = "IS_POST_LOGIN"

        enum class Screens {
            TRANSACTION_LIST,

        }
    }
}
