package com.sergiocruz.matematica

import timber.log.Timber

internal class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        return
    }
}