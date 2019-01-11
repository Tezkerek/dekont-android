package ro.ande.dekont.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ro.ande.dekont.ui.AuthActivity
import ro.ande.dekont.ui.MainActivity

@Module
abstract class ActivityModule {
    @ContributesAndroidInjector
    internal abstract fun contributeTransactionsActivity(): MainActivity

    @ContributesAndroidInjector
    internal abstract fun contributeLoginActivity(): AuthActivity
}