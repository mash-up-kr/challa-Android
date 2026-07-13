package com.happyhouse.challa.presentation.photodetail.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel

/**
 * 저장소 권한을 확인/요청하는 게이트
 * 실제 저장은 ViewModel이 수행하므로, 여기서는 권한이 확보됐을 때만 [onGranted]로 저장 대상을 넘긴다.
 */
@Composable
fun rememberPhotoSavePermissionGate(
    onDenied: () -> Unit,
    onGranted: (PhotoDetailUiModel) -> Unit,
): (PhotoDetailUiModel) -> Unit {
    val context = LocalContext.current
    val currentOnDenied by rememberUpdatedState(onDenied)
    val currentOnGranted by rememberUpdatedState(onGranted)

    var pendingPhoto by rememberSaveable { mutableStateOf<PhotoDetailUiModel?>(null) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            val photo = pendingPhoto
            pendingPhoto = null
            when {
                photo == null -> currentOnDenied()
                isGranted -> currentOnGranted(photo)
                else -> currentOnDenied()
            }
        }

    return remember {
        { photo ->
            if (context.isStorageAccessAllowed()) {
                currentOnGranted(photo)
            } else {
                pendingPhoto = photo
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }
}

private fun Context.isStorageAccessAllowed(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
        PackageManager.PERMISSION_GRANTED
