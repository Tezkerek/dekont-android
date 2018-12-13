package ro.ande.dekont.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ro.ande.dekont.vo.Transaction

@Database(
        entities = [Transaction::class],
        version = 3
)
@TypeConverters(Converters::class)
abstract class DekontDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}