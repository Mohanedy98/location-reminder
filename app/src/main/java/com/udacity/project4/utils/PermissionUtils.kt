package com.udacity.project4.utils


import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R

object PermissionUtils {
    val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    @TargetApi(29)
     fun foregroundAndBackgroundLocationPermissionApproved(context: Context): Boolean {
        val foregroundApproved =
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ))
        val backgroundApproved = if (runningQOrLater) {
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ))
        } else {
            true
        }
        return foregroundApproved && backgroundApproved
    }

    fun foregroundLocationPermissionApproved(context: Context): Boolean {
        val foregroundApproved =
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            )) ||    (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))

        return foregroundApproved
    }

    fun showSettingsSnackBar(view: View) {
        Snackbar.make(
            view,
            R.string.permission_denied_explanation,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.settings) {
                startActivity(
                    view.context,
                    Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    },
                    null,
                )
            }.show()
    }
}