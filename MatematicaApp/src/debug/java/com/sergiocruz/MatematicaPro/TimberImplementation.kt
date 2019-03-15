package com.sergiocruz.MatematicaPro

import timber.log.Timber

object TimberImplementation {

    private var debugTree: DebugTree? = null

    fun init() {
        // Avoid duplication
        if (debugTree == null) {
            debugTree = DebugTree()
        } else {
            Timber.uprootAll()
        }
        Timber.plant(debugTree!!)
    }
}