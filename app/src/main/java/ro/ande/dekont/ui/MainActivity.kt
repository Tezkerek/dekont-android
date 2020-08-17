package ro.ande.dekont.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
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

        // https://stackoverflow.com/a/62612502/4904553
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController = navHostFragment.navController

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

    companion object {
        const val INTENT_ARG_IS_POST_LOGIN = "IS_POST_LOGIN"
    }
}
