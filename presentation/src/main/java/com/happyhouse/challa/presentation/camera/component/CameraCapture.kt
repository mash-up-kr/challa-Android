package com.happyhouse.challa.presentation.camera.component

import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import timber.log.Timber
import java.util.concurrent.Executor

internal fun ImageCapture.capturePhoto(
    executor: Executor,
    onCaptureResult: (Boolean) -> Unit,
) {
    takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                image.close()
                onCaptureResult(true)
            }

            override fun onError(exception: ImageCaptureException) {
                Timber.e(exception)
                onCaptureResult(false)
            }
        },
    )
}
