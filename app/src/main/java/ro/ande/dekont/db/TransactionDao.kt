package ro.ande.dekont.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.threeten.bp.LocalDate
import ro.ande.dekont.vo.Transaction

@Dao
abstract class TransactionDao {
    @Insert
    abstract fun insert(vararg transactions: Transaction)

    @Insert
    abstract fun insert(transactions: List<Transaction>): Long

    @Update
    abstract fun update(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAndReplace(transactions: List<Transaction>)

    @Query("SELECT * FROM `transaction` WHERE id = :id")
    abstract fun getById(id: Int): Transaction

    @Query("SELECT * FROM `transaction` WHERE id = :id")
    abstract fun retrieveById(id: Int): LiveData<Transaction>

    @Query("SELECT * FROM `transaction` WHERE date >= :date")
    abstract fun retrieveSince(date: LocalDate): LiveData<List<Transaction>>
}