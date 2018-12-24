package ro.ande.dekont.repo

import androidx.lifecycle.LiveData
import ro.ande.dekont.AppExecutors
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
    fun retrieveAll(): LiveData<List<Category>> {
        return categoryDao.retrieveAll()
    }
}