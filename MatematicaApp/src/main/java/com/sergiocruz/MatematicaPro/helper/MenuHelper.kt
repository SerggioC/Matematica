package com.sergiocruz.MatematicaPro.helper

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.SuperscriptSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.sergiocruz.MatematicaPro.BuildConfig
import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.R.id.history
import com.sergiocruz.MatematicaPro.activity.MainActivity
import com.sergiocruz.MatematicaPro.allPermissionsGranted
import com.sergiocruz.MatematicaPro.getRuntimePermissions
import java.text.SimpleDateFormat
import java.util.*

/*****
 * Project Matematica
 * Package com.sergiocruz.Matematica.helper
 * Created by Sergio on 09/11/2016 21:11
 */

object MenuHelper : MainActivity.PermissionResultInterface {

    init {
        MainActivity.permissionResultInterface = this
    }

    private const val IMAGES_FOLDER = "Matematica Images"
    private const val SAVED_IMAGE_QUALITY = 90
    private var savedCallback: (() -> Unit)? = null

    override fun onPermissionResult(permitted: Boolean) {
        // if allowed, run the saved callback, that is,
        // continue with the operation that needs permission
        if (permitted) savedCallback?.invoke()
    }

    // returns image path
    fun saveViewToImage(theViewToSave: View, index: Int, drawWhiteBG: Boolean): String? {
        theViewToSave.isDrawingCacheEnabled = true
        var bitmap = Bitmap.createBitmap(theViewToSave.width, theViewToSave.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        if (drawWhiteBG) {
            canvas.drawColor(Color.WHITE, PorterDuff.Mode.ADD)
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        }
        theViewToSave.draw(canvas)
        bitmap = theViewToSave.drawingCache

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Calendar.getInstance().time)
        val fileName = "img" + timeStamp + "_" + index + ".jpg"
        val context = theViewToSave.context
        val pathName = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, fileName, context.getString(R.string.app_long_description) + BuildConfig.VERSION_NAME)
        return pathName
    }

    fun saveViewToImageAndOpenSnackbar(theViewToSave: View, index: Int, drawWhiteBG: Boolean) {
        val path = saveViewToImage(theViewToSave, index, drawWhiteBG)
        if (path.isNullOrEmpty()) {
            showCustomToast(theViewToSave.context, theViewToSave.context.getString(R.string.errorsavingimg), InfoLevel.ERROR)
        } else {
            openImagesFolderSnackbar(theViewToSave, R.string.image_saved, path)
        }
    }


    fun openImagesFolderSnackbar(view: View, @StringRes toastText: Int, imagePath: String) {
        val snack = Snackbar.make(view, toastText, Snackbar.LENGTH_LONG)
        val context = view.context
        snack.setAction(context.getString(R.string.open_folder)) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(imagePath), "image/jpeg")
            try {
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.open_folder)))
            } catch (e: java.lang.Exception) {
                showCustomToast(context, context.getString(R.string.error_no_file_manager), InfoLevel.ERROR)
            }
        }
        snack.setActionTextColor(ContextCompat.getColor(context, R.color.f_color8))
        snack.show()
    }

    fun removeHistory(activity: Activity) {
        val history: ViewGroup? = activity.findViewById(R.id.history)
        val childCount = history?.childCount
        if (childCount != null && childCount > 0) {
            history.removeAllViews()
            showCustomToast(activity, activity.getString(R.string.history_deleted))
        }
    }

    fun shareHistory(activity: Activity) {
        val historyView = activity.findViewById<ViewGroup>(history)
        val textViewsTagTexto = getViewsByTag(historyView, "texto")
        if (textViewsTagTexto.size > 0) {
            var finalText = ""
            for (i in textViewsTagTexto.indices) {
                if (textViewsTagTexto[i] is TextView) {
                    var textFromView =
                        (textViewsTagTexto[i] as TextView).text.toString() + "\n"
                    val ss = SpannableString((textViewsTagTexto[i] as TextView).text)
                    val spans = ss.getSpans(0, (textViewsTagTexto[i] as TextView).text.length, SuperscriptSpan::class.java)
                    for ((corr, span) in spans.withIndex()) {
                        val start = ss.getSpanStart(span) + corr
                        textFromView = textFromView.substring(0, start) + "^" +
                                textFromView.substring(start)
                    }
                    finalText += textFromView + "\n"
                }
            }

            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, activity.resources.getString(R.string.app_long_description) +
                    BuildConfig.VERSION_NAME + "\n" + finalText)
            sendIntent.type = "text/plain"
            activity.startActivity(Intent.createChooser(sendIntent, activity.resources.getString(R.string.app_name)))

        } else {
            val thetoast =
                Toast.makeText(activity, R.string.nothing_toshare, Toast.LENGTH_SHORT)
            thetoast.setGravity(Gravity.CENTER, 0, 0)
            thetoast.show()
        }
    }

    fun checkPermissionsWithCallback(activity: Activity, callback: () -> Unit): Unit {
        if (allPermissionsGranted(activity)) {
            callback()
        } else {
            savedCallback = callback
            getRuntimePermissions(activity)
        }
    }

    // Partilhar tudo como imagens
    fun shareHistoryImages(mActivity: Activity) {
        checkPermissionsWithCallback(mActivity) {
            val historyView = mActivity.findViewById<ViewGroup>(history)
            val childCount = historyView?.childCount

            if (childCount != null && childCount > 0) {
                val fileUris = ArrayList<Uri>(childCount)
                for (i in 0 until childCount) {
                    val cardAtIndex = historyView.getChildAt(i) as CardView
                    cardAtIndex.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.cardsColor))
                    val imagePath = saveViewToImage(cardAtIndex, i, false)
                    fileUris.add(Uri.parse(imagePath))
                }

                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND_MULTIPLE
                sendIntent.putExtra(Intent.EXTRA_TEXT, mActivity.resources.getString(R.string.app_long_description) + BuildConfig.VERSION_NAME + "\n")
                sendIntent.type = "image/jpeg"
                sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris)
                mActivity.startActivity(Intent.createChooser(sendIntent, mActivity.resources.getString(R.string.app_name)))
            } else {
                val theToast =
                    Toast.makeText(mActivity, R.string.nothing_toshare, Toast.LENGTH_SHORT)
                theToast.setGravity(Gravity.CENTER, 0, 0)
                theToast.show()
            }
        }

    }

    // Guardar todas as imagens
    fun saveHistoryImages(mActivity: Activity) {
        val historyView = mActivity.findViewById<ViewGroup>(history)
        val childCount = historyView?.childCount
        if (childCount != null && childCount > 0) {
            checkPermissionsWithCallback(mActivity) {
                val imagePaths: MutableList<String> = mutableListOf()
                for (i in 0 until childCount) {
                    val cardAtIndex = historyView.getChildAt(i) as CardView
                    cardAtIndex.setCardBackgroundColor(
                        ContextCompat.getColor(mActivity, R.color.cardsColor)
                    )
                    saveViewToImage(cardAtIndex, i, false)?.also {
                        imagePaths.add(it)
                    }
                }
                if (imagePaths.isNotEmpty()) {
                    openImagesFolderSnackbar(historyView, R.string.all_images_saved, imagePaths.get(0))
                } else {
                    val theToast = Toast.makeText(mActivity, mActivity.getString(R.string.errorsavingimg), Toast.LENGTH_SHORT)
                    theToast.setGravity(Gravity.CENTER, 0, 0)
                    theToast.show()
                }
            }
        } else {
            val theToast =
                Toast.makeText(mActivity, R.string.nothing_tosave, Toast.LENGTH_SHORT)
            theToast.setGravity(Gravity.CENTER, 0, 0)
            theToast.show()
        }
    }

    private fun getViewsByTag(root: ViewGroup, tag: String?): ArrayList<View> {
        val views = ArrayList<View>()
        val childCount = root.childCount
        for (i in 0 until childCount) {
            val child = root.getChildAt(i)
            if (child is ViewGroup) {
                views.addAll(getViewsByTag(child, tag))
            }
            val tagObj = child.getTag(R.id.texto)
            if (tagObj != null && tagObj == tag) {
                views.add(child)
            }
        }
        return views
    }

    fun expandIt(view: View, newHeight: Int?) {
        view.measure(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val targetHeight = newHeight ?: view.measuredHeight

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        view.layoutParams.height = 1
        view.visibility = View.VISIBLE
        val animation = object : Animation() {

            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                view.layoutParams.height = (targetHeight * interpolatedTime).toInt() + 1
                view.requestLayout()
            }

            override fun willChangeBounds() = true
        }

        var animating = false

        animation.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                animating = false
                view.setTag(R.id.expanded, true)
            }

            override fun onAnimationStart(animation: Animation?) {
                animating = true
            }
        })

        animation.duration =
            (targetHeight / view.context.resources.displayMetrics.density).toLong() * 3
        if (animating.not()) {
            view.startAnimation(animation)
        }
    }

    fun collapseIt(view: View) {
        val initialHeight = view.measuredHeight
        val animation = object : Animation() {

            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                if (interpolatedTime == 1f) {
                    view.visibility = View.GONE
                } else {
                    view.layoutParams.height = initialHeight -
                            (initialHeight * interpolatedTime).toInt()
                    view.requestLayout()
                }
            }

            override fun willChangeBounds() = true
        }
        var animating = false

        animation.duration =
            (initialHeight / view.context.resources.displayMetrics.density).toLong() * 3
        animation.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                animating = false
                view.setTag(R.id.expanded, false)
            }

            override fun onAnimationStart(animation: Animation?) {
                animating = true
            }
        })

        if (animating.not()) {
            view.startAnimation(animation)
        }
    }
}



