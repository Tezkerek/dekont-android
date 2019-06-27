package ro.ande.dekont.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_group_settings.*
import ro.ande.dekont.R
import ro.ande.dekont.di.Injectable
import ro.ande.dekont.viewmodel.GroupSettingsViewModel
import javax.inject.Inject

class GroupSettingsFragment : Fragment(), Injectable {
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var groupSettingsViewModel: GroupSettingsViewModel

    private var activeSnackbar: Snackbar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_settings, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        groupSettingsViewModel = ViewModelProviders.of(this, mViewModelFactory).get(GroupSettingsViewModel::class.java)

        this.view?.setOnTouchListener { _, _ -> true }
        setupButtonActions()

        groupSettingsViewModel.user.observe(this, Observer { userResource ->
            toggleProgressBar(false)

            if (userResource.isSuccess()) {
                if (userResource.data?.group == null) {
                    showOutsideGroupControls()
                }
            } else if (userResource.isError()) {
                activeSnackbar = Snackbar.make(this.group_settings_view!!, getString(R.string.general_error_prefix, userResource.message), Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_retry) { loadCurrentUser() }
                activeSnackbar?.show()
            }
        })
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        toggleProgressBar(true)
        groupSettingsViewModel.loadCurrentUser()
    }

    private fun toggleProgressBar(show: Boolean) {
        this.progress_bar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showOutsideGroupControls() {
        this.outside_group_controls.visibility = View.VISIBLE
    }

    private fun setupButtonActions() {
        this.join_group_button.setOnClickListener {  }
    }

    private fun showInviteCodeInputDialog() {

    }

    override fun onDestroy() {
        super.onDestroy()
        activeSnackbar?.dismiss()
    }
}