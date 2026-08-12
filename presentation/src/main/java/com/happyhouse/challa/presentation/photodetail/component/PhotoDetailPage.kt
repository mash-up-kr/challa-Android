package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import com.happyhouse.challa.presentation.photodetail.contract.PhotoReactionUiModel
import com.happyhouse.challa.presentation.photodetail.contract.ReactionEmoji
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

// Figma(390x844) 기준 카드 높이. 화면 폭에 비례시키면 폴더블·태블릿에서 화면 밖으로 넘쳐서 높이는 고정한다.
internal val PhotoCardHeight = 477.dp

private val PhotoShape = RoundedCornerShape(44.5.dp)

private val ProfileImageSize = 22.dp

private val PhotoDimBrush =
    Brush.verticalGradient(
        0f to Color.Black.copy(alpha = 0.6f),
        0.39518f to Color.Transparent,
    )

@Composable
fun PhotoDetailPage(
    photo: PhotoDetailUiModel,
    reactions: ImmutableList<PhotoReactionUiModel>,
    modifier: Modifier = Modifier,
) {
    // URL이 바뀌면 초기화되도록 imageUrl을 key로 둔다. 주소 자체를 못 받았으면 그릴 이미지가 없으니 바로 실패로 본다.
    var isLoadFailed by remember(photo.imageUrl) { mutableStateOf(photo.imageUrl == null) }

    Box(
        modifier =
            modifier
                .clip(PhotoShape)
                .background(ChallaTheme.colors.backgroundLevel2)
                .border(1.dp, ChallaTheme.colors.lineNormal, PhotoShape),
    ) {
        photo.imageUrl?.let { imageUrl ->
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = stringResource(R.string.photo_detail_photo_content_description),
                contentScale = ContentScale.Crop,
                onState = { state -> isLoadFailed = state is AsyncImagePainter.State.Error },
            )
        }

        Spacer(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(PhotoDimBrush),
        )

        if (isLoadFailed) {
            PhotoImageLoadFailure(modifier = Modifier.align(Alignment.Center))
        }

        PhotographerInfo(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp),
            photo = photo,
        )

        // 카드가 clip(PhotoShape)돼 있어 스티커는 사진 영역 안에서만 보인다.
        PhotoReactionOverlay(
            modifier = Modifier.fillMaxSize(),
            reactions = reactions,
        )
    }
}

/** 이미지 1장 로드 실패. 목록 전체 실패는 PhotoInfo.Error가 담당한다. */
@Composable
private fun PhotoImageLoadFailure(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            painter = painterResource(id = ChallaIcons.Error),
            contentDescription = null,
            tint = ChallaTheme.colors.labelDisable,
        )

        Text(
            text = stringResource(R.string.photo_detail_load_failure),
            color = ChallaTheme.colors.labelDisable,
            style = ChallaTheme.typography.bodySmall.medium,
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
            PhotographerAvatar(profileImageUrl = photo.photographerProfileImageUrl)

            Text(
                modifier = Modifier.padding(vertical = 2.dp),
                // 촬영자를 못 받아도 촬영 시각 줄과 균형이 맞도록 자리를 비우지 않고 대체 문구를 그린다.
                text = photo.photographer ?: stringResource(R.string.photo_detail_unknown_photographer),
                color = ChallaTheme.colors.labelNormal,
                style = ChallaTheme.typography.bodyMedium.medium,
            )
        }

        // 촬영 시각은 대체할 문구가 없어서, 못 받았으면 줄 자체를 그리지 않는다.
        photo.capturedDate?.let { capturedDate ->
            Text(
                text = capturedDate,
                color = ChallaTheme.colors.primary,
                style = ChallaTheme.typography.bodySmall.medium,
            )
        }
    }
}

/**
 * 촬영자 프로필 사진. 등록하지 않았거나 불러오지 못하면 기본 프로필 아이콘을 그린다.
 */
@Composable
private fun PhotographerAvatar(
    profileImageUrl: String?,
    modifier: Modifier = Modifier,
) {
    // URL이 바뀌면 다시 시도하도록 profileImageUrl을 key로 둔다.
    var isLoadFailed by remember(profileImageUrl) { mutableStateOf(false) }

    Box(
        modifier =
            modifier
                .size(ProfileImageSize)
                .clip(CircleShape)
                .background(ChallaTheme.colors.backgroundLevel2),
    ) {
        if (profileImageUrl == null || isLoadFailed) {
            Icon(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = ChallaIcons.Profile),
                contentDescription = null,
                tint = ChallaTheme.colors.lineNeutral,
            )
        } else {
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(profileImageUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onState = { state -> isLoadFailed = state is AsyncImagePainter.State.Error },
            )
        }
    }
}

@ComposePreview(showBackground = true, name = "PhotoDetailPage - 이미지 로드 실패")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoImageLoadFailurePreview() {
    PhotoImageLoadFailure()
}

@ComposePreview(showBackground = true, name = "PhotoDetailPage - 사진 주소·촬영자·시각 없음")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPageWithoutPhotoInfoPreview() {
    PhotoDetailPage(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(PhotoCardHeight),
        photo =
            PhotoDetailUiModel(
                id = 1L,
                imageUrl = null,
                photographer = null,
                photographerProfileImageUrl = null,
                capturedDate = null,
            ),
        reactions = persistentListOf(),
    )
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
                .height(PhotoCardHeight),
        photo =
            PhotoDetailUiModel(
                id = 1L,
                imageUrl = "",
                photographer = "나는야멋쟁이토마토",
                photographerProfileImageUrl = null,
                capturedDate = "2026. 7. 16. 14:34",
            ),
        reactions =
            persistentListOf(
                PhotoReactionUiModel(id = 0L, emoji = ReactionEmoji.HEART),
                PhotoReactionUiModel(id = 1L, emoji = ReactionEmoji.CLAP),
            ),
    )
}
