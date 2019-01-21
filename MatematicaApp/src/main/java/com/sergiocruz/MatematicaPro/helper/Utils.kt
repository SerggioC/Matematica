package com.sergiocruz.MatematicaPro.helper

import android.content.Context
import android.support.v7.app.AlertDialog
import com.sergiocruz.MatematicaPro.R

interface OnCancelBackgroundTask {
    fun onOperationCanceled(canceled: Boolean)
}

fun displayCancelDialogBox(context: Context, onCanceled: OnCancelBackgroundTask) {

    val alertDialogBuilder = AlertDialog.Builder(context)

    // set title
    alertDialogBuilder.setTitle(context.getString(R.string.primetable_title))

    // set dialog message
    alertDialogBuilder
        .setMessage(R.string.cancel_it)
        .setCancelable(true)
        .setPositiveButton(R.string.sim) { dialog, id ->
            onCanceled.onOperationCanceled(true)
            dialog.cancel()
        }
        .setNegativeButton(R.string.nao) { dialog, id -> dialog.cancel() }
    val alertDialog = alertDialogBuilder.create()        // create alert dialog
    alertDialog.show()                                   // show it
}