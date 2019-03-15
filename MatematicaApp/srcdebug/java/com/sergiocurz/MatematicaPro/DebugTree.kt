package com.sergiocruz.MatematicaPro

import com.crashlytics.android.Crashlytics

import timber.log.Timber

class DebugTree : Timber.DebugTree() {

    override fun createStackElementTag(element: StackTraceElement): String? {
        return String.format(
            "Sergio> %s; Method %s; Line %s",
            super.createStackElementTag(element),
            element.methodName,
            element.lineNumber
        )
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        Crashlytics.log(priority, tag, message)
    }
}
