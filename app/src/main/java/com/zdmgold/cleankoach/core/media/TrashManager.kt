package com.zdmgold.cleankoach.core.media

import android.app.PendingIntent
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import com.zdmgold.cleankoach.core.domain.model.CleanupResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrashManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun buildDeleteRequest(uris: List<Uri>): IntentSender? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        val pendingIntent: PendingIntent = MediaStore.createDeleteRequest(
            context.contentResolver,
            uris
        )
        return pendingIntent.intentSender
    }

    fun toRequest(intentSender: IntentSender): IntentSenderRequest =
        IntentSenderRequest.Builder(intentSender).build()

    fun buildCleanupResult(
        itemCount: Int,
        freedBytes: Long,
        cacheFreedBytes: Long
    ): CleanupResult = CleanupResult(
        deletedItemCount = itemCount,
        freedBytes = freedBytes,
        cacheClearedBytes = cacheFreedBytes,
        performedAt = System.currentTimeMillis()
    )
}
