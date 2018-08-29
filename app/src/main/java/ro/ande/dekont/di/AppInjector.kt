package ro.ande.dekont.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import dagger.android.AndroidInjection
import ro.ande.dekont.DekontApp

/**
 * Marks an activity as injectable.
 */
interface Injectable

object AppInjector {
    fun injectAll(app: DekontApp) {
        DaggerAppComponent.builder()
                .application(app)
                .build()
                .inject(app)

        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                injectComponents(activity)
            }

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityDestroyed(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}

            override fun onActivityStopped(activity: Activity) {}
        })
    }

    private fun injectComponents(activity: Activity) {
        if (activity is Injectable) {
            AndroidInjection.inject(activity)
        }
    }
}
