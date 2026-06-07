package com.happyhouse.challa.presentation.sample

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.GPUImageView
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.collections.get

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permLauncher.launch(Manifest.permission.CAMERA)
    }

    if (hasPermission) {
        CameraContent()
    } else {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("카메라 권한이 필요합니다", color = Color.White)
        }
    }
}

@Composable
private fun CameraContent() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val filters = remember(context) { loadFilmFilters(context) }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val selectedFilterIndexRef = remember { mutableStateOf(0) }
    selectedFilterIndexRef.value = selectedFilterIndex

    var isCapturing by remember { mutableStateOf(false) }
    var gpuImageView by remember { mutableStateOf<GPUImageView?>(null) }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val captureExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    LaunchedEffect(selectedFilterIndex, gpuImageView) {
        gpuImageView?.filter = filters[selectedFilterIndex].create()
    }

    DisposableEffect(lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()

            val resolutionSelector = ResolutionSelector.Builder()
                .setResolutionStrategy(
                    ResolutionStrategy(
                        Size(720, 1280),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                    )
                )
                .build()

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setResolutionSelector(resolutionSelector)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            analysis.setAnalyzer(analysisExecutor) { proxy ->
                proxy.use { proxy ->
                    val rotation = proxy.imageInfo.rotationDegrees
                    val bm = proxy.toBitmap().rotated(rotation)
                    val view = gpuImageView ?: return@setAnalyzer
                    view.post { view.setImage(bm) }
                }
            }

            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    analysis,
                    imageCapture
                )
            } catch (_: Exception) {
                // do nothing
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            analysisExecutor.shutdown()
            captureExecutor.shutdown()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        GPUImageView(ctx).apply {
                            setScaleType(GPUImage.ScaleType.CENTER_CROP)
                            setBackgroundColor(0f, 0f, 0f)
                            gpuImageView = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(count = filters.size) { index ->
                    val f = filters[index]
                    val selected = index == selectedFilterIndex
                    Button(
                        onClick = { selectedFilterIndex = index },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected)
                                MaterialTheme.colorScheme.primary
                            else
                                Color.DarkGray
                        )
                    ) {
                        Text(
                            f.label,
                            color = Color.White,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        if (isCapturing) return@Button
                        isCapturing = true
                        val filterIndex = selectedFilterIndexRef.value
                        imageCapture.takePicture(
                            captureExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val rotation = image.imageInfo.rotationDegrees
                                    val bm = image.toBitmap().rotated(rotation)
                                    image.close()
                                    val gpu = GPUImage(context).apply {
                                        setFilter(filters[filterIndex].create())
                                    }
                                    val filtered = gpu.getBitmapWithFilterApplied(bm)
                                    val uri = runCatching { saveImage(context, filtered) }
                                        .getOrNull()
                                    scope.launch {
                                        Toast.makeText(
                                            context,
                                            if (uri != null) "저장됨: $uri" else "저장 실패",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        isCapturing = false
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    scope.launch {
                                        Toast.makeText(
                                            context,
                                            "촬영 실패: ${exception.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        isCapturing = false
                                    }
                                }
                            }
                        )
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isCapturing) Color.Gray else Color.Red)
                    )
                }
            }
        }
    }
}
