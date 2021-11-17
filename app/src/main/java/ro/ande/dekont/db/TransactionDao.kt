package ro.ande.dekont.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ro.ande.dekont.vo.Transaction

@Dao
abstract class TransactionDao {
    @Insert
    abstract suspend fun insert(transaction: Transaction): Long

    @Insert
    abstract fun insert(transactions: List<Transaction>): List<Long>

    @Update
    abstract fun update(transaction: Transaction)

    @Query("DELETE FROM `transaction` WHERE id = :id")
    abstract suspend fun delete(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAndReplace(transactions: List<Transaction>)

    @Query("SELECT * FROM `transaction` WHERE id = :id")
    abstract fun getById(id: Int): Transaction

    @Query("SELECT * FROM `transaction` WHERE id = :id")
    abstract suspend fun retrieveById(id: Int): Transaction?

    @Query("SELECT * FROM `transaction` ORDER BY date DESC LIMIT :limit OFFSET :offset")
    abstract fun retrievePartial(offset: Int, limit: Int): Flow<List<Transaction>>
}