package ro.ande.dekont.viewmodel

import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import ro.ande.dekont.BaseActivity
import ro.ande.dekont.di.DaggerAppComponent

@MainThread
inline fun <reified VM : ViewModel> Fragment.injectableViewModel(): Lazy<VM> =
        viewModels(factoryProducer = { DaggerAppComponent.builder().application(this.activity!!.application).build().viewModelProviderFactory() })

@MainThread
inline fun <reified VM : ViewModel> BaseActivity.injectableViewModel(): Lazy<VM> =
        viewModels(factoryProducer = { DaggerAppComponent.builder().application(this.application).build().viewModelProviderFactory() })
