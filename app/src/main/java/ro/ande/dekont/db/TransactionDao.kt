package ro.ande.dekont.db

import androidx.lifecycle.LiveData
import androidx.room.*
import ro.ande.dekont.vo.Transaction

@Dao
abstract class TransactionDao {
    @Insert
    abstract fun insert(transactions: Transaction): Long

    @Insert
    abstract fun insert(transactions: List<Transaction>): List<Long>

    @Update
    abstract fun update(transaction: Transaction)

    @Query("DELETE FROM `transaction` WHERE id = :id")
    abstract fun delete(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAndReplace(transactions: List<Transaction>)

    @Query("SELECT * FROM `transaction` WHERE id = :id")
    abstract fun getById(id: Int): Transaction

    @Query("SELECT * FROM `transaction` WHERE id = :id")
    abstract fun retrieveById(id: Int): LiveData<Transaction>

    @Query("SELECT * FROM `transaction` ORDER BY date DESC LIMIT :limit OFFSET :offset")
    abstract fun retrievePartial(offset: Int, limit: Int): LiveData<List<Transaction>>
}