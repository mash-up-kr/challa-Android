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
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.presentation.R
import com.happyhouse.challa.presentation.designsystem.component.ChallaProfileImage
import com.happyhouse.challa.presentation.designsystem.icon.ChallaIcons
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import com.happyhouse.challa.presentation.photodetail.contract.PhotoDetailUiModel
import com.happyhouse.challa.presentation.photodetail.contract.PhotoReactionUiModel
import com.happyhouse.challa.presentation.photodetail.contract.ReactionBurstUiModel
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
    burst: ReactionBurstUiModel? = null,
) {
    // URL이 바뀌면 초기화되도록 imageUrl을 key로 둔다.
    var isLoadFailed by remember(photo.imageUrl) { mutableStateOf(false) }

    // 스티커가 사진 밖으로 넘칠 수 있어야 해서 clip은 바깥이 아닌 사진 카드에만 건다.
    Box(modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(PhotoShape)
                    .background(ChallaTheme.colors.backgroundLevel2)
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
                onState = { state -> isLoadFailed = state is AsyncImagePainter.State.Error },
            )

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
        }

        PhotoReactionOverlay(
            modifier = Modifier.fillMaxSize(),
            photoId = photo.id,
            reactions = reactions,
        )

        // 남기는 순간의 연출. 다른 사진의 연출이 넘어오지 않게 이 사진 것만 재생한다.
        PhotoReactionBurst(
            modifier = Modifier.fillMaxSize(),
            burst = burst?.takeIf { it.photoId == photo.id },
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
                text = photo.photographer,
                color = ChallaTheme.colors.labelNormal,
                style = ChallaTheme.typography.bodyMedium.medium,
            )
        }

        Text(
            text = photo.capturedDate,
            color = ChallaTheme.colors.primary,
            style = ChallaTheme.typography.bodySmall.medium,
        )
    }
}

/** 촬영자 프로필 사진 */
@Composable
private fun PhotographerAvatar(
    profileImageUrl: String?,
    modifier: Modifier = Modifier,
) {
    ChallaProfileImage(
        modifier = modifier.size(ProfileImageSize),
        profileImageUrl = profileImageUrl,
        backgroundColor = ChallaTheme.colors.backgroundLevel2,
        fallbackIconTint = ChallaTheme.colors.lineNeutral,
    )
}

@ComposePreview(showBackground = true, name = "PhotoDetailPage - 이미지 로드 실패")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoImageLoadFailurePreview() {
    PhotoImageLoadFailure()
}

@ComposePreview(showBackground = true, name = "PhotoDetailPage - 프로필 사진 없음")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoDetailPageWithoutProfileImagePreview() {
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
                photographerProfileImageUrl = "https://challa.example.com/profile.png",
                capturedDate = "2026. 7. 16. 14:34",
            ),
        reactions =
            persistentListOf(
                PhotoReactionUiModel(chatId = 0L, emoji = ReactionEmoji.HEART),
                PhotoReactionUiModel(chatId = 1L, emoji = ReactionEmoji.FIRE),
            ),
    )
}
