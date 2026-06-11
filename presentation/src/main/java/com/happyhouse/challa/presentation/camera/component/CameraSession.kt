package com.happyhouse.challa.presentation.camera.component

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import timber.log.Timber
import androidx.camera.core.Camera as CameraXCamera

@Composable
fun CameraSession(
    modifier: Modifier = Modifier,
    lensFacing: Int,
    onCameraBound: (CameraXCamera?) -> Unit,
    onFlashAvailabilityChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) { createPreviewView(context) }

    DisposableEffect(context, lifecycleOwner, previewView, lensFacing) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)
        var cameraProvider: ProcessCameraProvider? = null
        var isDisposed = false

        cameraProviderFuture.addListener(
            {
                runCatching {
                    cameraProviderFuture.get()
                }.onSuccess { provider ->
                    cameraProvider = provider
                    provider.unbindAll()

                    if (isDisposed) {
                        provider.unbindAll()
                    } else {
                        val boundCamera =
                            bindPreviewUseCase(
                                cameraProvider = provider,
                                lifecycleOwner = lifecycleOwner,
                                previewView = previewView,
                                lensFacing = lensFacing,
                            )
                        onCameraBound(boundCamera)
                        onFlashAvailabilityChanged(boundCamera.cameraInfo.hasFlashUnit())
                    }
                }.onFailure { throwable ->
                    onCameraBound(null)
                    onFlashAvailabilityChanged(false)
                    Timber.e(throwable)
                }
            },
            mainExecutor,
        )

        onDispose {
            isDisposed = true
            onCameraBound(null)
            onFlashAvailabilityChanged(false)
            cameraProvider?.unbindAll()
        }
    }

    ViewFinder(modifier = modifier) { previewModifier ->
        AndroidView(
            modifier = previewModifier,
            factory = { previewView },
        )
    }
}

private fun createPreviewView(context: Context): PreviewView =
    PreviewView(context).apply {
        scaleType = PreviewView.ScaleType.FILL_CENTER
    }

private fun bindPreviewUseCase(
    cameraProvider: ProcessCameraProvider,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    lensFacing: Int,
): CameraXCamera {
    val preview = createPreview(previewView)
    val cameraSelector = createCameraSelector(lensFacing)

    return cameraProvider.bindToLifecycle(
        lifecycleOwner,
        cameraSelector,
        preview,
    )
}

private fun createPreview(previewView: PreviewView): Preview =
    Preview.Builder()
        .build()
        .also { preview ->
            preview.surfaceProvider = previewView.surfaceProvider
        }

private fun createCameraSelector(lensFacing: Int): CameraSelector =
    CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()
