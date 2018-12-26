package ro.ande.dekont.repo

import androidx.lifecycle.LiveData
import ro.ande.dekont.AppExecutors
import ro.ande.dekont.api.ApiResponse
import ro.ande.dekont.api.DekontService
import ro.ande.dekont.db.CategoryDao
import ro.ande.dekont.vo.Category
import ro.ande.dekont.vo.Resource
import javax.inject.Inject

class CategoryRepository
@Inject constructor(
        private val appExecutors: AppExecutors,
        private val categoryDao: CategoryDao,
        private val dekontService: DekontService
) {
    fun loadAll(): LiveData<Resource<List<Category>>> {
        return object : NetworkBoundResource<List<Category>, List<Category>>(appExecutors) {
            override fun saveCallResult(result: List<Category>) {
                categoryDao.insert(result)
            }

            override fun shouldFetch(data: List<Category>?): Boolean = true

            override fun loadFromDb(): LiveData<List<Category>> = categoryDao.retrieveAll()

            override fun createCall(): LiveData<ApiResponse<List<Category>>> = dekontService.listCategories()
        }.asLiveData()
    }

    fun retrieveAll(): LiveData<List<Category>> {
        return categoryDao.retrieveAll()
    }
}