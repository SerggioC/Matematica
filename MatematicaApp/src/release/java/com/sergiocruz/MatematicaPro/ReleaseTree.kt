package com.sergiocruz.MatematicaPro

import com.crashlytics.android.Crashlytics

import timber.log.Timber

import android.util.Log.ERROR
import android.util.Log.WARN

internal class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == ERROR || priority == WARN)
            Crashlytics.log(priority, tag, message)
    }
}
