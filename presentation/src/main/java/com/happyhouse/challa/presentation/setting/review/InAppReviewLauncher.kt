package com.happyhouse.challa.presentation.setting.review

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import timber.log.Timber

internal class InAppReviewLauncher(
    private val activity: Activity?,
    private val reviewManager: ReviewManager,
) {
    private var isInProgress = false

    fun launch() {
        if (isInProgress) return

        val reviewActivity =
            activity?.takeUnless { it.isFinishing || it.isDestroyed }
                ?: return

        isInProgress = true
        reviewManager.requestReviewFlow().addOnCompleteListener { requestTask ->
            if (!requestTask.isSuccessful) {
                isInProgress = false
                Timber.w(requestTask.exception, "인앱 리뷰 정보를 요청하지 못했습니다.")
                return@addOnCompleteListener
            }

            if (reviewActivity.isFinishing || reviewActivity.isDestroyed) {
                isInProgress = false
                return@addOnCompleteListener
            }

            reviewManager
                .launchReviewFlow(reviewActivity, requestTask.result)
                .addOnCompleteListener { launchTask ->
                    isInProgress = false
                    launchTask.exception?.let { throwable ->
                        Timber.w(throwable, "인앱 리뷰 흐름을 완료하지 못했습니다.")
                    }
                }
        }
    }
}

@Composable
internal fun rememberInAppReviewLauncher(): InAppReviewLauncher {
    val context = LocalContext.current

    return remember(context) {
        InAppReviewLauncher(
            activity = context.findActivity(),
            reviewManager = ReviewManagerFactory.create(context.applicationContext),
        )
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
