package ro.ande.dekont

import android.app.Activity
import androidx.fragment.app.Fragment
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import ro.ande.dekont.di.AppInjector
import ro.ande.dekont.di.DaggerAppComponent
import javax.inject.Inject

class DekontApp : DaggerApplication(), HasActivityInjector, HasSupportFragmentInjector {
    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentInjector

    @Inject lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>
    @Inject lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

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

    override fun activityInjector(): DispatchingAndroidInjector<Activity> = dispatchingActivityInjector
}