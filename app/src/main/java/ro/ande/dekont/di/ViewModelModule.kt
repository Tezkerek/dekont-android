package ro.ande.dekont.di

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ro.ande.dekont.viewmodel.LoginViewModel
import ro.ande.dekont.viewmodel.TransactionsViewModel
import ro.ande.dekont.viewmodel.ViewModelFactory

@Module
abstract class ViewModelModule {
    @Binds @IntoMap
    @ViewModelKey(LoginViewModel::class)
    internal abstract fun bindLoginViewModel(loginViewModel: LoginViewModel): ViewModel

    @Binds @IntoMap
    @ViewModelKey(TransactionsViewModel::class)
    internal abstract fun bindTransactionsViewModel(transactionsViewModel: TransactionsViewModel): ViewModel

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}