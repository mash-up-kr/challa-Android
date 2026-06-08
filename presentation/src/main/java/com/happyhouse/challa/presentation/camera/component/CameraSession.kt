package com.happyhouse.challa.presentation.camera.component

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
import androidx.lifecycle.compose.LocalLifecycleOwner
import timber.log.Timber
import androidx.camera.core.Camera as CameraXCamera

@Composable
fun CameraSession(
    lensFacing: Int,
    onCameraBound: (CameraXCamera?) -> Unit,
    onFlashAvailabilityChanged: (Boolean) -> Unit,
    content: @Composable (cameraPreview: @Composable (Modifier) -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView =
        remember {
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        }

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

                    val preview =
                        Preview
                            .Builder()
                            .build()
                            .also { preview ->
                                preview.surfaceProvider = previewView.surfaceProvider
                            }
                    val cameraSelector =
                        CameraSelector
                            .Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                    if (isDisposed) {
                        provider.unbindAll()
                    } else {
                        val boundCamera =
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
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

    content { modifier ->
        AndroidView(
            modifier = modifier,
            factory = { previewView },
        )
    }
}
