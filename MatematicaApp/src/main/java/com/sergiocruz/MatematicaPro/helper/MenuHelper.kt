package com.sergiocruz.MatematicaPro.helper

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.text.SpannableString
import android.text.style.SuperscriptSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.sergiocruz.MatematicaPro.BuildConfig
import com.sergiocruz.MatematicaPro.R

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar

import com.sergiocruz.MatematicaPro.R.id.history

/*****
 * Project Matematica
 * Package com.sergiocruz.Matematica.helper
 * Created by Sergio on 09/11/2016 21:11
 */

class MenuHelper : ActivityCompat.OnRequestPermissionsResultCallback {


    // TODO manage permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(
            "Sergio>",
            this.toString() + "\nonRequestPermissionsResult:\nrequestCode=\n" + requestCode
        )
        for (i in permissions.indices) {
            Log.i(
                "Sergio>",
                this.toString() + "\nonRequestPermissionsResult:\npermissions=\n" + permissions[i]
            )
        }
        for (i in grantResults.indices) {
            Log.i(
                "Sergio>",
                this.toString() + "\nonRequestPermissionsResult:\ngrantResults=\n" + grantResults[i]
            )
        }
        // PackageManager.PERMISSION_GRANTED;
        // PackageManager.PERMISSION_DENIED;

    }

    companion object {
        private val IMAGES_FOLDER = "Matematica Images"
        private val SAVED_IMAGE_QUALITY = 90
        // Storage Permissions
        private val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        /**
         * Checks if the app has permission to write to device storage
         * If the app does not has permission then the user will be prompted to grant permissions
         *
         * @param activity
         */
        fun verifyStoragePermissions(activity: Activity) {
            // Check if we have write permission
            val permission = ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // if we don't have permission prompt the user to give permission
                ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
                )
            }
        }

        fun saveViewToImage(theViewToSave: View, index: Int, drawWhiteBG: Boolean): String? {
            var pathname: String?
            theViewToSave.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(
                theViewToSave.width,
                theViewToSave.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            if (drawWhiteBG) {
                canvas.drawColor(Color.WHITE, PorterDuff.Mode.ADD)
            } else {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            }
            theViewToSave.draw(canvas)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, SAVED_IMAGE_QUALITY, byteArrayOutputStream)
            val folder =
                File(Environment.getExternalStorageDirectory().toString() + File.separator + IMAGES_FOLDER)
            try {
                if (!folder.exists()) folder.mkdirs()
                val timeStamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().time)
                pathname = Environment.getExternalStorageDirectory().toString() + File.separator +
                        IMAGES_FOLDER + File.separator + "img" + timeStamp + "_" + index + ".jpg"
                val image_file = File(pathname)
                image_file.createNewFile()
                val fileOutputStream = FileOutputStream(image_file)
                fileOutputStream.write(byteArrayOutputStream.toByteArray())
            } catch (e: Exception) {
                e.printStackTrace()
                pathname = null
            }

            return pathname
        }

        fun openFolder_Snackbar(mActivity: Activity, toastText: String) {
            val snack = Snackbar.make(
                mActivity.findViewById(android.R.id.content),
                toastText,
                Snackbar.LENGTH_LONG
            )
            snack.setAction(mActivity.getString(R.string.open_folder)) { v ->
                val intent = Intent(Intent.ACTION_VIEW)
                val uri =
                    Uri.parse(Environment.getExternalStorageDirectory().path.toString() + File.separator + IMAGES_FOLDER + File.separator)
                intent.setDataAndType(uri, "*/*")
                try {
                    mActivity.startActivity(
                        Intent.createChooser(
                            intent,
                            mActivity.getString(R.string.open_folder)
                        )
                    )
                } catch (e: android.content.ActivityNotFoundException) {
                    Toast.makeText(
                        mActivity,
                        "Error. Please install a file manager.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            snack.setActionTextColor(ContextCompat.getColor(mActivity, R.color.f_color8))
            snack.show()
        }

        fun remove_history(activity: Activity) {
            val history = activity.findViewById<ViewGroup>(R.id.history)
            if (history.childCount > 0)
                history.removeAllViews()
            val thetoast = Toast.makeText(activity, R.string.history_deleted, Toast.LENGTH_SHORT)
            thetoast.setGravity(Gravity.CENTER, 0, 0)
            thetoast.show()
        }

        fun share_history(activity: Activity) {
            val history_view = activity.findViewById<ViewGroup>(history)
            val textViews_withTAG_texto = getViewsByTag(history_view, "texto")
            if (textViews_withTAG_texto.size > 0) {
                var text_fromTextViews_final = ""
                for (i in textViews_withTAG_texto.indices) {
                    if (textViews_withTAG_texto[i] is TextView) {
                        var text_fromTextView =
                            (textViews_withTAG_texto[i] as TextView).text.toString() + "\n"
                        val ss = SpannableString((textViews_withTAG_texto[i] as TextView).text)
                        val spans = ss.getSpans(
                            0,
                            (textViews_withTAG_texto[i] as TextView).text.length,
                            SuperscriptSpan::class.java
                        )
                        var corr = 0
                        for (span in spans) {
                            val start = ss.getSpanStart(span) + corr
                            text_fromTextView = text_fromTextView.substring(0, start) + "^" +
                                    text_fromTextView.substring(start)
                            corr++
                        }
                        text_fromTextViews_final += text_fromTextView + "\n"
                    }
                }

                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(
                    Intent.EXTRA_TEXT, activity.resources.getString(R.string.app_long_description) +
                            BuildConfig.VERSION_NAME + "\n" + text_fromTextViews_final
                )
                sendIntent.type = "text/plain"
                activity.startActivity(
                    Intent.createChooser(
                        sendIntent,
                        activity.resources.getString(R.string.app_name)
                    )
                )

            } else {
                val thetoast =
                    Toast.makeText(activity, R.string.nothing_toshare, Toast.LENGTH_SHORT)
                thetoast.setGravity(Gravity.CENTER, 0, 0)
                thetoast.show()
            }
        }

        // Partilhar todas as imagens
        fun share_history_images(mActivity: Activity) {
            verifyStoragePermissions(mActivity)
            val history_view = mActivity.findViewById<ViewGroup>(history)
            val childCount = history_view.childCount

            if (childCount > 0) {
                val file_uris = ArrayList<Uri>(childCount)
                for (i in 0 until childCount) {
                    val cardAtIndex = history_view.getChildAt(i) as CardView
                    cardAtIndex.setCardBackgroundColor(
                        ContextCompat.getColor(
                            mActivity,
                            R.color.cardsColor
                        )
                    )
                    val img_path = saveViewToImage(cardAtIndex, i, false)
                    file_uris.add(Uri.parse(img_path))
                }

                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND_MULTIPLE
                sendIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    mActivity.resources.getString(R.string.app_long_description) +
                            BuildConfig.VERSION_NAME + "\n"
                )
                sendIntent.type = "image/jpeg"
                sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, file_uris)
                mActivity.startActivity(
                    Intent.createChooser(
                        sendIntent,
                        mActivity.resources.getString(R.string.app_name)
                    )
                )
            } else {
                val thetoast =
                    Toast.makeText(mActivity, R.string.nothing_toshare, Toast.LENGTH_SHORT)
                thetoast.setGravity(Gravity.CENTER, 0, 0)
                thetoast.show()
            }
        }

        // Guardar todas as imagens
        fun save_history_images(mActivity: Activity) {
            verifyStoragePermissions(mActivity)
            val history_view = mActivity.findViewById<ViewGroup>(history)
            val childCount = history_view.childCount
            if (childCount > 0) {
                var img_path: String? = null
                for (i in 0 until childCount) {
                    val cardAtIndex = history_view.getChildAt(i) as CardView
                    cardAtIndex.setCardBackgroundColor(
                        ContextCompat.getColor(
                            mActivity,
                            R.color.cardsColor
                        )
                    )
                    img_path = saveViewToImage(cardAtIndex, i, false)
                }
                if (img_path != null) {
                    MenuHelper.openFolder_Snackbar(
                        mActivity,
                        mActivity.getString(R.string.all_images_saved)
                    )
                } else {
                    val thetoast = Toast.makeText(
                        mActivity,
                        mActivity.getString(R.string.errorsavingimg),
                        Toast.LENGTH_SHORT
                    )
                    thetoast.setGravity(Gravity.CENTER, 0, 0)
                    thetoast.show()
                }


            } else {
                val thetoast =
                    Toast.makeText(mActivity, R.string.nothing_tosave, Toast.LENGTH_SHORT)
                thetoast.setGravity(Gravity.CENTER, 0, 0)
                thetoast.show()
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

        fun expandIt(v: View) {
            v.measure(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val targetHeight = v.measuredHeight
            // Older versions of android (pre API 21) cancel animations for views with a height of 0.
            v.layoutParams.height = 1
            v.visibility = View.VISIBLE
            val a = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    v.layoutParams.height = if (interpolatedTime == 1f)
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    else
                        (targetHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }
            // 1dp/ms
            a.duration =
                    (targetHeight / v.context.resources.displayMetrics.density).toInt().toLong()
            v.startAnimation(a)
        }

        fun collapseIt(v: View) {
            val initialHeight = v.measuredHeight
            val a = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    if (interpolatedTime == 1f) {
                        v.visibility = View.GONE
                    } else {
                        v.layoutParams.height = initialHeight -
                                (initialHeight * interpolatedTime).toInt()
                        v.requestLayout()
                    }
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }
            // 1dp/ms
            a.duration =
                    (initialHeight / v.context.resources.displayMetrics.density).toInt().toLong()
            v.startAnimation(a)
        }
    }


}
