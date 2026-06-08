package com.happyhouse.challa.presentation.camera.component

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.happyhouse.challa.presentation.R
import timber.log.Timber
import androidx.camera.core.Camera as CameraXCamera

@Composable
fun ViewFinder(
    modifier: Modifier = Modifier,
    lensFacing: Int,
    onCameraBound: (CameraXCamera?) -> Unit,
    onFlashAvailabilityChanged: (Boolean) -> Unit,
) {
    Box(modifier = modifier.border(width = 1.dp, color = Color(0xFF444444))) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            lensFacing = lensFacing,
            onCameraBound = onCameraBound,
            onFlashAvailabilityChanged = onFlashAvailabilityChanged,
        )
    }
}

@Composable
fun MockViewFinder(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .border(width = 1.dp, color = Color(0xFF444444))
                .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.camera_viewfinder_preview_label),
            color = Color(0xFF4A4A4A),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    lensFacing: Int,
    onCameraBound: (CameraXCamera?) -> Unit,
    onFlashAvailabilityChanged: (Boolean) -> Unit,
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

    AndroidView(
        modifier = modifier,
        factory = { previewView },
    )
}
