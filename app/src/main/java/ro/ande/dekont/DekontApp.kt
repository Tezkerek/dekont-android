package ro.ande.dekont

import android.app.Activity
import androidx.fragment.app.Fragment
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import ro.ande.dekont.di.AppInjector
import ro.ande.dekont.di.DaggerAppComponent
import ro.ande.dekont.viewmodel.ViewModelFactory
import javax.inject.Inject

class DekontApp : DaggerApplication(), HasAndroidInjector {
    @Inject lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>
    @Inject lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject lateinit var viewModelProviderFactory: ViewModelFactory

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent.builder()
                .application(this)
                .build()
                .inject(this)

        AppInjector.injectAll(this)

        AndroidThreeTen.init(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = DaggerAppComponent.builder().application(this).build()
}