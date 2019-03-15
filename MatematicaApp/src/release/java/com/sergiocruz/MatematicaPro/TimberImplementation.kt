package com.sergiocruz.MatematicaPro

import timber.log.Timber

object TimberImplementation {

    private var releaseTree: ReleaseTree? = null

    fun init() {
        if (releaseTree == null) {
            releaseTree = ReleaseTree()
        } else {
            Timber.uprootAll()
        }
        Timber.plant(releaseTree!!)
    }

}