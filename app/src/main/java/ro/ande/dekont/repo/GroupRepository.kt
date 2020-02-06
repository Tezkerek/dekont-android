package ro.ande.dekont.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ro.ande.dekont.AppExecutors
import ro.ande.dekont.api.*
import ro.ande.dekont.vo.Group
import ro.ande.dekont.vo.Resource
import javax.inject.Inject

class GroupRepository @Inject constructor(
        private val appExecutors: AppExecutors,
        private val dekontService: DekontService
) {

    private val _currentUserGroup = MutableLiveData<Resource<Group>>()
    val currentUserGroup: LiveData<Resource<Group>> = _currentUserGroup

    suspend fun fetchCurrentUserGroup() {
        val response = dekontService.retrieveCurrentUserGroup()
        when (response) {
            is ApiSuccessResponse -> _currentUserGroup.value = Resource.success(response.body)
            is ApiErrorResponse -> _currentUserGroup.value = Resource.error(response.getFirstError(), null)
        }
    }

    suspend fun joinGroup(inviteCode: String): ApiResponse<Void> =
            dekontService.joinGroup(GroupJoinRequest(inviteCode))
}