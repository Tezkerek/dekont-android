package ro.ande.dekont.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_transaction_list.*
import ro.ande.dekont.BaseActivity
import ro.ande.dekont.R
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.viewmodel.MainViewModel
import ro.ande.dekont.vo.Transaction
import javax.inject.Inject

class MainActivity : BaseActivity(), Injectable,
        TransactionListFragment.OnTransactionClickListener,
        TransactionListFragment.OnAddTransactionFabClickListener,
        TransactionEditorFragment.OnTransactionEditFinishedListener {
    @Inject lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(this.toolbar)

        /* UI Setup */
        // Show drawer button on toolbar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_white_24)
        }
        setupNavigationDrawer()

        /* ViewModel Setup */
        mainViewModel = ViewModelProviders.of(this, mViewModelFactory).get(MainViewModel::class.java)
        setupLoginCheck()

        // Post-login actions
        if (this.intent.getBooleanExtra(INTENT_ARG_IS_POST_LOGIN, false)) executePostLogin()

        // On first creation, add transaction list fragment
        if (savedInstanceState == null) {
            openTransactionList()
        }
    }

    private fun setupNavigationDrawer() {
        this.nav_drawer.setNavigationItemSelectedListener { item ->
            item.isChecked = true
            this.drawer_layout.closeDrawers()

            when (item.itemId) {
                R.id.nav_logout -> performLogout()
                R.id.nav_group -> openGroupSettings()
            }

            true
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
        
    }

    /** Show the transaction list fragment */
    private fun openTransactionList() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TransactionListFragment(), TransactionListFragment.TAG)
                .commit()
    }

    /** Open a screen containing a detailed view of the transaction */
    private fun openTransactionDetail(id: Int) {
        val fragment = TransactionDetailFragment()
        fragment.arguments = Bundle().also {
            it.putInt(TransactionDetailFragment.ARG_TRANSACTION_ID, id)
        }

        addAnimatedFragmentToBackStack(fragment)
    }

    /** Show the empty transaction editor fragment */
    private fun openNewTransactionEditor() {
        val fragment = TransactionEditorFragment()
        val args = Bundle()
        args.putInt(TransactionEditorFragment.ARG_ACTION, TransactionEditorFragment.ACTION_CREATE)
        fragment.arguments = args

        addAnimatedFragmentToBackStack(fragment, TransactionEditorFragment.TAG)
    }

    private fun openGroupSettings() {
        addAnimatedFragmentToBackStack(GroupSettingsFragment())
    }

    private fun addAnimatedFragmentToBackStack(fragment: Fragment, tag: String? = null) {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, 0, 0, R.anim.slide_out_right)
                .add(R.id.fragment_container, fragment, tag)
                .addToBackStack(null)
                .commit()
    }

    override fun onTransactionClick(id: Int) {
        openTransactionDetail(id)
    }

    override fun onAddTransactionFabClick(fab: FloatingActionButton) {
        openNewTransactionEditor()
    }

    override fun onTransactionEditFinished(transaction: Transaction) {
        // Go back to the list and refresh it
        supportFragmentManager.popBackStack()
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
                .setNegativeButton(R.string.dialog_action_cancel) { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Open drawer when pressing the menu button
                this.drawer_layout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
