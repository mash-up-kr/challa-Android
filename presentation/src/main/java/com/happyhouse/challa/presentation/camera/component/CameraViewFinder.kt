package com.happyhouse.challa.presentation.camera.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
    cameraPreview: @Composable (Modifier) -> Unit,
) {
    Box(modifier = modifier.border(width = 1.dp, color = Color(0xFF444444))) {
        cameraPreview(Modifier.fillMaxSize())
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
