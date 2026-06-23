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

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is PhotoDetailSideEffect.SavePhotoToDevice -> {
                    // TODO: 권한 처리 + 실제 저장은 후속 이슈에서 구현
                    val result = savePhotoToMediaStore(context, effect.imageUrl)
                    viewModel.onIntent(PhotoDetailIntent.PhotoSaveResult(result.isSuccess))
                }

                is PhotoDetailSideEffect.ShowSaveResult -> {
                    val message = if (effect.isSuccess) saveSuccessMessage else saveFailureMessage
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    PhotoDetailScreen(
        modifier = Modifier.fillMaxSize(),
        state = state,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
        onMoreClick = {},
    )
}
