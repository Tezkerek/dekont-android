package ro.ande.dekont.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ro.ande.dekont.LoginActivity
import ro.ande.dekont.TransactionsActivity

@Module
abstract class ActivityModule {
    @ContributesAndroidInjector
    internal abstract fun contributeTransactionsActivity(): TransactionsActivity

    @ContributesAndroidInjector
    internal abstract fun contributeLoginActivity(): LoginActivity
}