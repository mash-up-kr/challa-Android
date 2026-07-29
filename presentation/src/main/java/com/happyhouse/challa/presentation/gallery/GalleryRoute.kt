package com.happyhouse.challa.presentation.gallery

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.gallery.contract.GallerySideEffect

@Composable
fun GalleryRoute(
    roomId: Long,
    onBackClick: () -> Unit,
    onPhotoClick: (Long) -> Unit,
    viewModel: GalleryViewModel =
        hiltViewModel<GalleryViewModel, GalleryViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(roomId)
            },
        ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is GallerySideEffect.NavigateToPhotoDetail -> onPhotoClick(effect.photoId)
            }
        }
    }

    GalleryScreen(
        modifier = Modifier.fillMaxSize(),
        state = state,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
    )
}
