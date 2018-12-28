package ro.ande.dekont.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ro.ande.dekont.vo.Category

@Dao
abstract class CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(categories: List<Category>): List<Long>

    @Query("SELECT * FROM `category`")
    abstract fun retrieveAll(): LiveData<List<Category>>
}