package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

internal const val PHOTO_DETAIL_ASPECT_RATIO = 358f / 477f

private val PhotoShape = RoundedCornerShape(44.5.dp)

private val PhotoDimBrush =
    Brush.verticalGradient(
        0f to Color.Black.copy(alpha = 0.6f),
        0.39518f to Color.Transparent,
    )

@Composable
fun PhotoDetailPage(
    photo: PhotoDetailUiModel,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(PhotoShape)
                .border(1.dp, ChallaTheme.colors.lineNormal, PhotoShape),
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(photo.imageUrl)
                    .crossfade(true)
                    .build(),
            contentDescription = stringResource(R.string.photo_detail_photo_content_description),
            contentScale = ContentScale.Crop,
        )

        Spacer(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(PhotoDimBrush),
        )

        PhotographerInfo(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp),
            photo = photo,
        )
    }
}

@Composable
private fun PhotographerInfo(
    photo: PhotoDetailUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // TODO: 프로필 이미지 API 연동 전까지 쓰는 placeholder
            Icon(
                modifier =
                    Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(ChallaTheme.colors.backgroundLevel2),
                painter = painterResource(id = ChallaIcons.Profile),
                contentDescription = null,
                tint = ChallaTheme.colors.lineNeutral,
            )

            Text(
                text = photo.photographer,
                color = ChallaTheme.colors.labelNormal,
                style = ChallaTheme.typography.bodyMedium.medium,
            )
        }

        Text(
            text = photo.capturedDate,
            color = ChallaTheme.colors.primaryYellow,
            style = ChallaTheme.typography.bodySmall.medium,
        )
    }
}

@ComposePreview(showBackground = true)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPagePreview() {
    PhotoDetailPage(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(PHOTO_DETAIL_ASPECT_RATIO),
        photo =
            PhotoDetailUiModel(
                id = 1L,
                imageUrl = "",
                photographer = "나는야멋쟁이토마토",
                capturedDate = "2026. 7.16. 14:34",
            ),
    )
}
