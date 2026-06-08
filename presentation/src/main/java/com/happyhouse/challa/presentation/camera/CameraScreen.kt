package com.happyhouse.challa.presentation.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import timber.log.Timber
import androidx.camera.core.Camera as CameraXCamera
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun CameraScreen(
    permissionState: CameraPermissionState,
    onRequestPermissionClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        when (permissionState) {
            CameraPermissionState.Checking -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            CameraPermissionState.Granted -> {
                CameraContent(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .systemBarsPadding(),
                    onBackClick = onBackClick,
                )
            }

            CameraPermissionState.Denied -> {
                CameraTopBar(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .systemBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                    onBackClick = onBackClick,
                    remainingCount = 24,
                    totalCount = 24,
                    isFlashOn = false,
                    isFlashEnabled = false,
                    onFlashClick = {},
                )
                CameraPermissionGuide(
                    modifier = Modifier.align(Alignment.Center),
                    onRequestPermissionClick = onRequestPermissionClick,
                )
            }
        }
    }
}

@Composable
private fun CameraContent(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
) {
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var isFlashOn by remember { mutableStateOf(false) }
    var hasFlashUnit by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<CameraXCamera?>(null) }

    LaunchedEffect(camera, isFlashOn, hasFlashUnit) {
        camera?.cameraControl?.enableTorch(isFlashOn && hasFlashUnit)
    }

    CameraContentLayout(
        modifier = modifier,
        isFlashOn = isFlashOn,
        isFlashEnabled = hasFlashUnit,
        onBackClick = onBackClick,
        onFlashClick = {
            if (hasFlashUnit) {
                isFlashOn = !isFlashOn
            }
        },
        onSwitchCameraClick = {
            isFlashOn = false
            lensFacing =
                if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
        },
        onShutterClick = {},
    ) { viewFinderModifier ->
        ViewFinder(
            modifier = viewFinderModifier,
            lensFacing = lensFacing,
            onCameraBound = { boundCamera ->
                camera = boundCamera
            },
            onFlashAvailabilityChanged = { isAvailable ->
                hasFlashUnit = isAvailable
                if (!isAvailable) {
                    isFlashOn = false
                }
            },
        )
    }
}

@Composable
private fun CameraContentLayout(
    modifier: Modifier = Modifier,
    isFlashOn: Boolean,
    isFlashEnabled: Boolean,
    onBackClick: () -> Unit,
    onFlashClick: () -> Unit,
    onSwitchCameraClick: () -> Unit,
    onShutterClick: () -> Unit,
    viewFinder: @Composable (Modifier) -> Unit,
) {
    val shutterDescription = stringResource(R.string.camera_shutter_description)
    val switchDescription = stringResource(R.string.camera_switch_description)

    Column(
        modifier = modifier,
    ) {
        CameraTopBar(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .padding(horizontal = 28.dp, vertical = 14.dp),
            onBackClick = onBackClick,
            remainingCount = 12,
            totalCount = 24,
            isFlashOn = isFlashOn,
            isFlashEnabled = isFlashEnabled,
            onFlashClick = onFlashClick,
        )
        viewFinder(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        CameraBottomBar(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(136.dp)
                    .padding(horizontal = 56.dp, vertical = 16.dp),
            shutterDescription = shutterDescription,
            switchDescription = switchDescription,
            onSwitchCameraClick = onSwitchCameraClick,
            onShutterClick = onShutterClick,
        )
    }
}

@Composable
private fun ViewFinder(
    modifier: Modifier = Modifier,
    lensFacing: Int,
    onCameraBound: (CameraXCamera?) -> Unit,
    onFlashAvailabilityChanged: (Boolean) -> Unit,
) {
    Box(
        modifier =
            modifier
                .border(width = 1.dp, color = Color(0xFF444444))
                .clip(RoundedCornerShape(0.dp)),
    ) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            lensFacing = lensFacing,
            onCameraBound = onCameraBound,
            onFlashAvailabilityChanged = onFlashAvailabilityChanged,
        )
    }
}

@Composable
private fun PreviewViewFinder(modifier: Modifier = Modifier) {
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

@Composable
private fun CameraTopBar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    remainingCount: Int,
    totalCount: Int,
    isFlashOn: Boolean,
    isFlashEnabled: Boolean,
    onFlashClick: () -> Unit,
) {
    Box(modifier = modifier) {
        CameraIconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            text = stringResource(R.string.camera_close_icon),
            textColor = Color.White,
            onClick = onBackClick,
        )
        CameraCounter(
            modifier = Modifier.align(Alignment.Center),
            remainingCount = remainingCount,
            totalCount = totalCount,
        )
        CameraIconButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            text = stringResource(R.string.camera_flash_icon),
            textColor =
                when {
                    !isFlashEnabled -> Color(0xFF666666)
                    isFlashOn -> Color(0xFFFFD000)
                    else -> Color(0xFFFFB300)
                },
            onClick = onFlashClick,
        )
    }
}

@Composable
private fun CameraCounter(
    modifier: Modifier = Modifier,
    remainingCount: Int,
    totalCount: Int,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF2B2B2B))
                .padding(horizontal = 18.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.camera_remaining_counter, remainingCount, totalCount),
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CameraBottomBar(
    modifier: Modifier = Modifier,
    shutterDescription: String,
    switchDescription: String,
    onSwitchCameraClick: () -> Unit,
    onShutterClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        CameraIconButton(
            modifier = Modifier.width(64.dp),
            text = stringResource(R.string.camera_switch_icon),
            textColor = Color.White,
            contentDescription = switchDescription,
            onClick = onSwitchCameraClick,
        )
        ShutterButton(
            contentDescription = shutterDescription,
            onClick = onShutterClick,
        )
        PreviewEmptyLabel(modifier = Modifier.width(64.dp))
    }
}

@Composable
private fun ShutterButton(
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(
                    role = Role.Button,
                    onClickLabel = contentDescription,
                    onClick = onClick,
                )
                .semantics {
                    this.contentDescription = contentDescription
                },
    )
}

@Composable
private fun PreviewEmptyLabel(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = stringResource(R.string.camera_preview_empty),
        color = Color(0xFF8D8D8D),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        lineHeight = 18.sp,
    )
}

@Composable
private fun CameraIconButton(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color,
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .size(56.dp)
                .clip(CircleShape)
                .clickable(
                    role = Role.Button,
                    onClickLabel = contentDescription,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CameraPermissionGuide(
    modifier: Modifier = Modifier,
    onRequestPermissionClick: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.camera_permission_required_title),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.camera_permission_required_description),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            modifier = Modifier.padding(top = 24.dp),
            onClick = onRequestPermissionClick,
        ) {
            Text(text = stringResource(R.string.camera_permission_request_button))
        }
    }
}

@ComposePreview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraScreenDeniedPreview() {
    CameraScreen(
        permissionState = CameraPermissionState.Denied,
        onRequestPermissionClick = {},
        onBackClick = {},
    )
}

@ComposePreview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun CameraScreenGrantedPreview() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        CameraContentLayout(
            modifier = Modifier.fillMaxSize(),
            isFlashOn = false,
            isFlashEnabled = true,
            onBackClick = {},
            onFlashClick = {},
            onSwitchCameraClick = {},
            onShutterClick = {},
            viewFinder = { modifier ->
                PreviewViewFinder(modifier = modifier)
            },
        )
    }
}
