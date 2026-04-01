package com.rsav.githubPublicRepoBrowser

import android.app.Application
import android.os.StrictMode
import com.rsav.githubPublicRepoBrowser.util.L
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        L.i(TAG, "Application created")

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectNetwork()
                    .detectCustomSlowCalls()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects()
                    .detectActivityLeaks()
                    .detectLeakedSqlLiteObjects()
                    .penaltyLog()
                    .build()
            )
            L.i(TAG, "StrictMode enabled (logging only)")
        }
    }

    companion object {
        private const val TAG = "App"
    }
}
