package com.zdmgold.cleankoach.core.media

import android.Manifest
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun hasFullMediaAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            granted(Manifest.permission.READ_MEDIA_IMAGES) &&
                granted(Manifest.permission.READ_MEDIA_VIDEO)
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            granted(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                granted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            granted(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun hasPartialMediaAccess(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return false
        val partial = ContextCompat.checkSelfPermission(
            context,
            "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
        ) == PackageManager.PERMISSION_GRANTED
        return partial && !hasFullMediaAccess()
    }

    private fun granted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
