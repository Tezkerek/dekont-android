package ro.ande.dekont.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_group_settings.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ro.ande.dekont.R
import ro.ande.dekont.api.ApiErrorResponse
import ro.ande.dekont.api.ApiSuccessResponse
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

        // Attempt to load the user information
        groupSettingsViewModel.user.observe(this, Observer { userResource ->
            toggleProgressBar(false)


            if (userResource.isSuccess()) {
                // Check if user is in a group or not
                if (userResource.data?.group == null) {
                    toggleNotInGroupControls(true)
                    toggleGroupScreen(false)
                } else {
                    toggleNotInGroupControls(false)
                    toggleGroupScreen(true)
                }
            } else if (userResource.isError()) {
                activeSnackbar = Snackbar.make(this.group_settings_view!!, getString(R.string.general_error_prefix, userResource.message), Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_retry) { loadCurrentUser() }
                        .also { it.show() }
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

    private fun toggleNotInGroupControls(show: Boolean) {
        this.not_in_group_controls.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun toggleGroupScreen(show: Boolean) {
        this.group_screen.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setupButtonActions() {
        this.join_group_button.setOnClickListener { showInviteCodeInputScreen() }
    }

    private fun showInviteCodeInputScreen() {
        toggleNotInGroupControls(false)

        // Inflate input view
        val codeInputView = layoutInflater.inflate(R.layout.view_group_join_code_input, this.group_settings_view, false)
        this.group_settings_view.addView(codeInputView)

        // Function for removing the invite screen
        fun removeInputView() {
            this.group_settings_view.removeView(codeInputView)
            toggleNotInGroupControls(true)
        }

        // Set up submit button
        codeInputView.findViewById<Button>(R.id.invite_code_submit_button).setOnClickListener {
            // Send join request
            val inviteCodeEditText = codeInputView.findViewById<EditText>(R.id.invite_code_input)
            val inviteCode = inviteCodeEditText.text.toString()
            toggleProgressBar(true)
            lifecycleScope.launch {
                val response = groupSettingsViewModel.joinGroup(inviteCode)
                withContext(Dispatchers.Main) {
                    when (response) {
                        is ApiSuccessResponse -> {
                            removeInputView()
                            loadCurrentUser()
                        }
                        is ApiErrorResponse -> {
                            inviteCodeEditText.error = response.getFirstError()
                        }
                    }
                    toggleProgressBar(false)
                }
            }
        }

        // Set up cancel button
        codeInputView.findViewById<Button>(R.id.invite_code_cancel_button).setOnClickListener {
            removeInputView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activeSnackbar?.dismiss()
    }
}