package com.sergiocruz.MatematicaPro

import android.app.Application
import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        installLeakCanary()
        TimberImplementation.init()
    }

    private fun installLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
    }

}