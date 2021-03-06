package com.sergiocruz.matematica.helper

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.sergiocruz.matematica.R
import com.sergiocruz.matematica.databinding.GradientSeparatorBinding
import com.sergiocruz.matematica.helper.InfoLevel.*
import java.text.DecimalFormat

interface OnCancelBackgroundTask {
    fun onOperationCanceled(canceled: Boolean)
}

fun displayCancelDialogBox(context: Context, @StringRes title: Int, onCanceled: OnCancelBackgroundTask) {

    val alertDialogBuilder = AlertDialog.Builder(context)

    // set title
    alertDialogBuilder.setTitle(title)

    // set dialog message
    alertDialogBuilder
            .setMessage(R.string.cancel_it)
            .setCancelable(true)
            .setPositiveButton(R.string.sim) { dialog, _ ->
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

fun cancelAsyncTask(task: AsyncTask<*, *, *>?, context: Context?): Boolean {
    return if (task?.status == AsyncTask.Status.RUNNING) {
        task.cancel(true)
        showCustomToast(context, context?.getString(R.string.canceled_op), WARNING)
        true
    } else false
}

//fun showCustomToast(context: Context, toastText: String, icon_RID: Int, text_color_Res_Id: Int, duration: Int? = Toast.LENGTH_LONG) {
fun showCustomToast(
        context: Context?,
        toastText: String?,
        level: InfoLevel = INFO,
        duration: Int = Toast.LENGTH_LONG
) {
    if (context == null) return
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
        CONFIRM -> R.mipmap.ic_ok
        WARNING -> R.mipmap.ic_warn
        ERROR -> R.mipmap.ic_error
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

//Hide the keyboard
fun hideKeyboard(activity: Activity?) {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
}

fun showKeyboard(activity: Activity?) {
    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(activity.currentFocus, 0)
}

fun getGradientSeparator(context: Context, showPerformance: Boolean, startTime: Long, a: String? = null, b: String? = null): View {
    val gradientBinding = GradientSeparatorBinding.inflate(LayoutInflater.from(context))
    gradientBinding.gradientSeparator.visibility = if (showPerformance) View.VISIBLE else View.GONE
    if (showPerformance) {
        val formatter1 = DecimalFormat("#.###")
        val elapsed = context.getString(R.string.performance) + " " + formatter1.format((System.nanoTime() - startTime) / 1000000000.0) + "s"
        gradientBinding.gradientSeparator.text = elapsed
    }
    return gradientBinding.root
}
