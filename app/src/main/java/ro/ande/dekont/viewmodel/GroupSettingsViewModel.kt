package ro.ande.dekont.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ro.ande.dekont.api.ApiResponse
import ro.ande.dekont.repo.GroupRepository
import ro.ande.dekont.repo.UserRepository
import ro.ande.dekont.vo.Group
import ro.ande.dekont.vo.Resource
import ro.ande.dekont.vo.User
import javax.inject.Inject

class GroupSettingsViewModel
@Inject constructor(
    app: Application,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) : AndroidViewModel(app) {
    val group: LiveData<Resource<Group>> = groupRepository.currentUserGroup

    val user: LiveData<Resource<User>> = userRepository.currentUser

    fun loadCurrentGroup() {
        viewModelScope.launch {
            groupRepository.fetchCurrentUserGroup()
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            userRepository.fetchCurrentUser()
        }
    }

    suspend fun joinGroup(inviteCode: String): ApiResponse<Void> =
        groupRepository.joinGroup(inviteCode)
}