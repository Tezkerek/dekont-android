package ro.ande.dekont.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ro.ande.dekont.db.CategoryDao
import ro.ande.dekont.db.DekontDatabase
import ro.ande.dekont.db.TransactionDao
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class, ApiModule::class])
class AppModule {
    @Provides
    @Singleton
    fun provideDatabase(app: Application): DekontDatabase =
            Room.databaseBuilder(app, DekontDatabase::class.java, "dekont.db")
                    .fallbackToDestructiveMigration()
                    .build()

    @Provides
    @Singleton
    fun provideTransactionDao(db: DekontDatabase): TransactionDao = db.transactionDao()

    @Provides
    @Singleton
    fun provideCategoryDao(db: DekontDatabase): CategoryDao = db.categoryDao()
}
