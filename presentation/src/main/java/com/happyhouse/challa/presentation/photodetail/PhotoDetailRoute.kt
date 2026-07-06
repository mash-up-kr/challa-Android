package com.happyhouse.challa.presentation.photodetail

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailIntent
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailSideEffect
import com.happyhouse.challa.presentation.photodetail.permission.rememberPhotoSavePermissionGate

@Composable
fun PhotoDetailRoute(
    roomId: Long,
    photoId: Long,
    onBackClick: () -> Unit,
    viewModel: PhotoDetailViewModel =
        hiltViewModel<PhotoDetailViewModel, PhotoDetailViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(roomId = roomId, initialPhotoId = photoId)
            },
        ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val saveSuccessMessage = stringResource(R.string.photo_detail_save_success)
    val saveFailureMessage = stringResource(R.string.photo_detail_save_failure)

    // 저장 실행/결과는 ViewModel이 담당하고, 여기서는 저장소 권한만 확인한 뒤 저장 인텐트를 올린다.
    val requestSave =
        rememberPhotoSavePermissionGate(
            onDenied = { Toast.makeText(context, saveFailureMessage, Toast.LENGTH_SHORT).show() },
            onGranted = { photo -> viewModel.onIntent(PhotoDetailIntent.PhotoSave(photo)) },
        )

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            val message =
                when (effect) {
                    PhotoDetailSideEffect.SaveSucceeded -> saveSuccessMessage
                    PhotoDetailSideEffect.SaveFailed -> saveFailureMessage
                }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    PhotoDetailScreen(
        modifier = Modifier.fillMaxSize(),
        state = state,
        onIntent = viewModel::onIntent,
        onSaveClick = requestSave,
        onBackClick = onBackClick,
        onMoreClick = {},
    )
}
