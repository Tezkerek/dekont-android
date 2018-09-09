package ro.ande.dekont.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ro.ande.dekont.TransactionListFragment

@Module
abstract class FragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeTransactionListFragment(): TransactionListFragment
}