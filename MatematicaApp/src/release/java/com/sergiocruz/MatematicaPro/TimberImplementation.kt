package com.sergiocruz.MatematicaPro

import timber.log.Timber

object TimberImplementation {

    private val releaseTree: ReleaseTree by lazy { ReleaseTree() }

    fun init() {
        Timber.uprootAll()
        Timber.plant(releaseTree)
    }

}