package ro.ande.dekont.repo

import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.doAsync
import ro.ande.dekont.AppExecutors
import ro.ande.dekont.api.*
import ro.ande.dekont.vo.Group
import ro.ande.dekont.vo.Resource
import javax.inject.Inject

class GroupRepository @Inject constructor(
        private val appExecutors: AppExecutors,
        private val dekontService: DekontService
) {

    fun retrieveCurrentUserGroup(): LiveData<Resource<Group>> {
        val groupLiveData: MutableLiveData<Resource<Group>> = MutableLiveData()

        appExecutors.networkIO().doAsync {
            runBlocking {
                val response = dekontService.retrieveCurrentUserGroup()
                when (response) {
                    is ApiSuccessResponse -> {
                        groupLiveData.postValue(Resource.success(response.body))
                    }
                    is ApiErrorResponse -> {
                        groupLiveData.postValue(Resource.error(response.getFirstError(), null))
                    }
                }
            }
        }

        return groupLiveData
    }

    fun joinGroup(inviteCode: String): Deferred<ApiResponse<Void>> =
            dekontService.joinGroup(GroupJoinRequest(inviteCode))
}