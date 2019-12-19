package ro.ande.dekont.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.Deferred
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
): AndroidViewModel(app) {
    val group: LiveData<Resource<Group>>
        get() = _group

    val user: LiveData<Resource<User>>
        get() = _user

    private val _group = MediatorLiveData<Resource<Group>>()
    private val _user = MediatorLiveData<Resource<User>>()

    fun loadCurrentGroup() {
        _group.addSource(groupRepository.retrieveCurrentUserGroup()) { group ->
            _group.value = group
        }
    }

    fun loadCurrentUser() {
        _user.addSource(userRepository.retrieveCurrentUser()) { user ->
            _user.value = user
        }
    }

    fun joinGroup(inviteCode: String): Deferred<ApiResponse<Void>> =
            groupRepository.joinGroup(inviteCode)
}