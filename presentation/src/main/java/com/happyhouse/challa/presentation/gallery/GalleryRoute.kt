package com.happyhouse.challa.presentation.gallery

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.happyhouse.challa.presentation.R
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
    val snackbarHostState = remember { SnackbarHostState() }
    val printWaitingMessage = stringResource(R.string.gallery_print_waiting_message)

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is GallerySideEffect.NavigateToPhotoDetail -> onPhotoClick(effect.photoId)
                GallerySideEffect.PrintWaiting -> snackbarHostState.showSnackbar(printWaitingMessage)
            }
        }
    }

    GalleryScreen(
        modifier = Modifier.fillMaxSize(),
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
    )
}
