package com.sergiocruz.MatematicaPro

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private const val PERMISSION_REQUEST_CODE = 1
private val PERMISSIONS_ARRAY = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

fun allPermissionsGranted(activity: Activity): Boolean {
    for (permission in PERMISSIONS_ARRAY) {
        if (isPermissionGranted(permission, activity).not()) {
            return false
        }
    }
    return true
}

fun getRuntimePermissions(activity: Activity) {
    val allNeededPermissions: ArrayList<String> = ArrayList(0)
    for (permission in PERMISSIONS_ARRAY) {
        if (isPermissionGranted(permission, activity).not()) {
            allNeededPermissions.add(permission)
        }
    }

    if (allNeededPermissions.isNotEmpty()) {
        ActivityCompat.requestPermissions(
            activity, allNeededPermissions.toTypedArray(),
            PERMISSION_REQUEST_CODE
        )
    }
}

fun isPermissionGranted(permission: String, activity: Activity) =
    ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED

/** Read and return manifest uses-permission fields
 * Will return merged manifest permissions
 * */
@SuppressLint("unused")
fun getRequiredPermissionsFromManifest(activity: Activity): Array<String?> {
    return try {
        val info = activity.packageManager
            .getPackageInfo(activity.packageName, PackageManager.GET_PERMISSIONS)
        val permissions = info.requestedPermissions
        if (permissions != null && permissions.isNotEmpty()) {
            permissions
        } else {
            arrayOfNulls(0)
        }
    } catch (e: Exception) {
        arrayOfNulls(0)
    }
}
