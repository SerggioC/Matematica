package com.sergiocruz.MatematicaPro.helper

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
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


    private fun openImagesFolderSnackbar(view: View, @StringRes toastText: Int, imagePath: String) {
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

    fun removeResultsFromLayout(activity: Activity) {
        val history: ViewGroup? = activity.findViewById(R.id.history)
        val childCount = history?.childCount
        if (childCount != null && childCount > 0) {
            history.removeAllViews()
            showCustomToast(activity, activity.getString(R.string.history_deleted))
        }
    }

    fun shareHistory(activity: Activity, withExplanations: Boolean) {
        val historyView = activity.findViewById<ViewGroup>(history)
        val finalText = historyView.getTextFromTextViews(withExplanations)
        if (finalText.isNotEmpty()) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, activity.resources.getString(R.string.app_long_description) + BuildConfig.VERSION_NAME + "\n" + finalText)
            sendIntent.type = "text/plain"
            activity.startActivity(Intent.createChooser(sendIntent, activity.resources.getString(R.string.app_name)))

        } else {
            val toast = Toast.makeText(activity, R.string.nothing_toshare, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }

    fun checkPermissionsWithCallback(activity: Activity, callback: () -> Unit) {
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
                    openImagesFolderSnackbar(historyView, R.string.all_images_saved, imagePaths[0])
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

}



