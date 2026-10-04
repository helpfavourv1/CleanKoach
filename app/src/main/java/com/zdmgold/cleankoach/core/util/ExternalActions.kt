package com.zdmgold.cleankoach.core.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.google.android.play.core.review.ReviewManagerFactory
import com.zdmgold.cleankoach.BuildConfig
import com.zdmgold.cleankoach.R
import com.zdmgold.cleankoach.core.AppLinks

/** Hands-off to other apps (browser, mail, store, share sheet). Every call fails soft with a toast. */
object ExternalActions {

    fun Context.findActivity(): Activity? {
        var current: Context? = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    fun shareApp(context: Context) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text, AppLinks.PLAY_URL))
        }
        runCatching {
            context.startActivity(
                Intent.createChooser(send, context.getString(R.string.settings_share_app))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun openUrl(context: Context, url: String) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, R.string.toast_no_browser, Toast.LENGTH_SHORT).show()
        }
    }

    fun openStorePage(context: Context) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(AppLinks.PLAY_MARKET_URI))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: ActivityNotFoundException) {
            openUrl(context, AppLinks.PLAY_URL)
        }
    }

    fun emailSupport(context: Context) {
        val body = context.getString(
            R.string.support_mail_body,
            Build.VERSION.RELEASE,
            "${Build.MANUFACTURER} ${Build.MODEL}"
        )
        val mail = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(AppLinks.SUPPORT_EMAIL))
            putExtra(
                Intent.EXTRA_SUBJECT,
                context.getString(R.string.support_mail_subject, BuildConfig.VERSION_NAME)
            )
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(mail)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, R.string.toast_no_mail, Toast.LENGTH_SHORT).show()
        }
    }

    /** Google decides whether the review sheet actually appears; failures are silent by design. */
    fun requestInAppReview(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnSuccessListener { info ->
            if (!activity.isFinishing) manager.launchReviewFlow(activity, info)
        }
    }
}
