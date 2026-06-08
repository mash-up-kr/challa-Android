package com.happyhouse.challa.presentation.camera.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyhouse.challa.presentation.R

@Composable
fun ViewFinder(
    modifier: Modifier = Modifier,
    previewContent: @Composable (Modifier) -> Unit,
) {
    ViewFinderFrame(modifier = modifier) {
        previewContent(Modifier.fillMaxSize())
    }
}

@Composable
fun MockViewFinder(modifier: Modifier = Modifier) {
    ViewFinderFrame(
        modifier = modifier.background(Color.Black),
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
private fun ViewFinderFrame(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.border(width = 1.dp, color = Color(0xFF444444)),
        contentAlignment = contentAlignment,
        content = content,
    )
}
