package com.sergiocruz.MatematicaPro

import timber.log.Timber

internal class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        return
    }
}