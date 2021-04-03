package com.sergiocruz.matematica

import timber.log.Timber

class DebugTree : Timber.DebugTree() {

    override fun createStackElementTag(element: StackTraceElement): String {
        return String.format(
            "Sergio> %s; Method %s; Line %s",
            super.createStackElementTag(element),
            element.methodName,
            element.lineNumber
        )
    }



}