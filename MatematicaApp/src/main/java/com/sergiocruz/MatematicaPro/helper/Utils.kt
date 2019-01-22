package com.sergiocruz.MatematicaPro.helper

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.helper.InfoLevel.*

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

enum class InfoLevel {
    INFO, CONFIRM, WARNING, ERROR
}

//fun showCustomToast(context: Context, toastText: String, icon_RID: Int, text_color_Res_Id: Int, duration: Int? = Toast.LENGTH_LONG) {
fun showCustomToast(
    context: Context?,
    toastText: String,
    level: InfoLevel = INFO,
    duration: Int = Toast.LENGTH_LONG
) {
    val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    if (inflater == null) {
        Toast.makeText(context, toastText, duration).show()
        return
    }
    val layout = inflater.inflate(R.layout.custom_toast, null)
    val text: TextView = layout.findViewById(R.id.toast_layout_text)
    text.text = toastText
    val textColorResId = when (level) {
        INFO -> android.R.color.holo_blue_dark
        CONFIRM -> android.R.color.holo_green_light
        WARNING -> android.R.color.holo_orange_dark
        ERROR -> android.R.color.holo_red_dark
    }
    val iconResId = when (level) {
        INFO -> R.mipmap.ic_info
        CONFIRM -> R.drawable.ic_ok
        WARNING -> R.drawable.ic_warn
        ERROR -> R.drawable.ic_error
    }
    text.setTextColor(ContextCompat.getColor(context, textColorResId))
    val imageV: ImageView = layout.findViewById(R.id.toast_img)
    imageV.setImageResource(iconResId)
    val theCustomToast = Toast(context)
    theCustomToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
    theCustomToast.duration = duration
    theCustomToast.view = layout
    theCustomToast.show()
}