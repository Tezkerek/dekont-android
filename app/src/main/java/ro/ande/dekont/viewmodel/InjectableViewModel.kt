package ro.ande.dekont.viewmodel

import android.app.Application
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import ro.ande.dekont.BaseActivity
import ro.ande.dekont.DekontApp

@MainThread
inline fun <reified VM : ViewModel> Fragment.injectableViewModel(): Lazy<VM> =
    viewModels(factoryProducer = {
        getFactoryProducer(this.requireActivity().application)
    })

@MainThread
inline fun <reified VM : ViewModel> BaseActivity.injectableViewModel(): Lazy<VM> =
    viewModels(factoryProducer = {
        getFactoryProducer(this.application)
    })

fun getFactoryProducer(app: Application) =
    when (app) {
        is DekontApp -> app.viewModelProviderFactory
        else -> throw IllegalArgumentException("The application must be an instance of ${DekontApp::class}")
    }
