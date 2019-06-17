package ro.ande.dekont.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ro.ande.dekont.ui.GroupSettingsFragment
import ro.ande.dekont.ui.TransactionDetailFragment
import ro.ande.dekont.ui.TransactionEditorFragment
import ro.ande.dekont.ui.TransactionListFragment

@Module
abstract class FragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeTransactionListFragment(): TransactionListFragment

    @ContributesAndroidInjector
    abstract fun contributeTransactionEditorFragment(): TransactionEditorFragment

    @ContributesAndroidInjector
    abstract fun contributeTransactionDetailFragment(): TransactionDetailFragment

    @ContributesAndroidInjector
    abstract fun contributeGroupSettingsFragment(): GroupSettingsFragment
}