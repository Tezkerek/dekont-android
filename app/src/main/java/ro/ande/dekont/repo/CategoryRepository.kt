package ro.ande.dekont.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import ro.ande.dekont.api.ApiErrorResponse
import ro.ande.dekont.api.ApiSuccessResponse
import ro.ande.dekont.api.DekontService
import ro.ande.dekont.db.CategoryDao
import ro.ande.dekont.util.CachedNetworkData
import ro.ande.dekont.util.NetworkState
import ro.ande.dekont.vo.Category
import javax.inject.Inject

class CategoryRepository
@Inject constructor(
        private val categoryDao: CategoryDao,
        private val dekontService: DekontService
) {
    fun loadAll(): CachedNetworkData<List<Category>> {
        val cachedData = categoryDao.retrieveAll().distinctUntilChanged()

        val networkState: Flow<NetworkState> = flow {
            emit(NetworkState.loading())

            when (val response = dekontService.listCategories()) {
                is ApiSuccessResponse -> {
                    categoryDao.insert(response.body)
                    emit(NetworkState.success(isExhausted = true))
                }
                is ApiErrorResponse -> {
                    emit(NetworkState.error(response.getFirstError()))
                }
            }
        }.distinctUntilChanged()

        return CachedNetworkData(cachedData, networkState)
    }

    fun retrieveAll(): Flow<List<Category>> {
        return categoryDao.retrieveAll()
    }
}