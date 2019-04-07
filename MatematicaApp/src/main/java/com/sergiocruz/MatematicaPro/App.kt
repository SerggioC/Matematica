package com.sergiocruz.MatematicaPro

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        TimberImplementation.init()
    }

}