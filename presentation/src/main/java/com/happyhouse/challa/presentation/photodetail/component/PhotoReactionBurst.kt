package com.happyhouse.challa.presentation.photodetail.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.IntOffset
import com.happyhouse.challa.domain.model.ReactionEmoji
import com.happyhouse.challa.presentation.designsystem.preview.ChallaPreviewWrapper
import com.happyhouse.challa.presentation.photodetail.contract.REACTION_BURST_DURATION_MILLIS
import com.happyhouse.challa.presentation.photodetail.contract.ReactionBurstUiModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

private const val BURST_PARTICLE_COUNT = 18

/** 파티클 크기(사진 폭 대비). 자리에 남는 스티커보다 훨씬 작다. */
private const val BURST_PARTICLE_WIDTH_RATIO = 0.12f

/** 사진 중심에서 흩어지는 거리(사진 크기 대비) */
private const val MIN_SPREAD = 0.15f
private const val MAX_SPREAD = 0.55f

private const val MAX_PARTICLE_TILT_DEGREES = 40f

/** 퍼지는 동안 커졌다가 제자리 크기로 돌아온다. */
private const val PARTICLE_START_SCALE = 0.4f

/** 이 비율을 지나면 서서히 사라진다. */
private const val FADE_START_PROGRESS = 0.55f

private data class BurstParticle(
    val angleRadians: Double,
    val spread: Float,
    val tiltDegrees: Float,
    /** 파티클마다 조금씩 늦게 출발해 한꺼번에 튀지 않게 한다. */
    val startDelay: Float,
)

/**
 * 반응을 남기는 순간 이모지가 사진 위로 쏟아지는 연출.
 *
 * [burst]의 id가 바뀔 때마다 처음부터 다시 재생한다. 같은 이모지를 연달아 남겨도
 * id가 달라 매번 새로 터진다. 연출이 끝나면 자리에 스티커만 남는다.
 */
@Composable
fun PhotoReactionBurst(
    burst: ReactionBurstUiModel?,
    modifier: Modifier = Modifier,
) {
    if (burst == null) return

    BoxWithConstraints(modifier = modifier) {
        val widthPx = constraints.maxWidth
        val heightPx = constraints.maxHeight
        val particlePx = (widthPx * BURST_PARTICLE_WIDTH_RATIO).roundToInt()
        val particleSize = with(LocalDensity.current) { particlePx.toDp() }

        val particles = remember(burst.id) { burstParticles(burst.id) }
        val progress = remember(burst.id) { Animatable(0f) }

        LaunchedEffect(burst.id) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = REACTION_BURST_DURATION_MILLIS.toInt(), easing = LinearEasing),
            )
        }

        val centerX = (widthPx - particlePx) / 2f
        val centerY = (heightPx - particlePx) / 2f

        // 진행값은 람다 안에서 읽는다. 컴포지션에서 읽으면 프레임마다 파티클 전부가 재구성된다.
        particles.forEach { particle ->
            Image(
                modifier =
                    Modifier
                        .size(particleSize)
                        .offset {
                            val distance =
                                MIN_SPREAD + (particle.spread - MIN_SPREAD) * easeOut(particle.progressAt(progress.value))
                            IntOffset(
                                x = (centerX + cos(particle.angleRadians).toFloat() * distance * widthPx).roundToInt(),
                                y = (centerY + sin(particle.angleRadians).toFloat() * distance * heightPx).roundToInt(),
                            )
                        }.graphicsLayer {
                            val particleProgress = particle.progressAt(progress.value)
                            val particleScale = PARTICLE_START_SCALE + (1f - PARTICLE_START_SCALE) * easeOut(particleProgress)

                            scaleX = particleScale
                            scaleY = particleScale
                            rotationZ = particle.tiltDegrees * particleProgress
                            // 아직 출발하지 않은 파티클은 투명하게 둔다.
                            alpha = if (particleProgress <= 0f) 0f else fadeAlpha(particleProgress)
                        },
                painter = painterResource(id = burst.emoji.drawableRes),
                contentDescription = null,
            )
        }
    }
}

/** 파티클이 자기 지연 시간을 지난 뒤부터 0 → 1로 진행한다. */
private fun BurstParticle.progressAt(elapsed: Float): Float {
    if (elapsed <= startDelay) return 0f
    return ((elapsed - startDelay) / (1f - startDelay)).coerceIn(0f, 1f)
}

private fun easeOut(progress: Float): Float = 1f - (1f - progress) * (1f - progress)

private fun fadeAlpha(progress: Float): Float =
    if (progress < FADE_START_PROGRESS) {
        1f
    } else {
        1f - (progress - FADE_START_PROGRESS) / (1f - FADE_START_PROGRESS)
    }

/** 같은 연출을 다시 그려도 모양이 흔들리지 않게 id를 seed로 쓴다. */
private fun burstParticles(burstId: Long): List<BurstParticle> {
    val random = Random(burstId)
    return List(BURST_PARTICLE_COUNT) {
        BurstParticle(
            angleRadians = random.nextDouble(0.0, 2 * PI),
            spread = random.nextFloat() * (MAX_SPREAD - MIN_SPREAD) + MIN_SPREAD,
            tiltDegrees = (random.nextFloat() * 2f - 1f) * MAX_PARTICLE_TILT_DEGREES,
            startDelay = random.nextFloat() * 0.25f,
        )
    }
}

@ComposePreview(showBackground = true, widthDp = 358, heightDp = 477)
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoReactionBurstPreview() {
    PhotoReactionBurst(
        modifier = Modifier.fillMaxSize(),
        burst = ReactionBurstUiModel(id = 1L, photoId = 1L, emoji = ReactionEmoji.EYES),
    )
}

@ComposePreview(showBackground = true, widthDp = 358, heightDp = 477, name = "PhotoReactionBurst - 없음")
@PreviewWrapper(wrapper = ChallaPreviewWrapper::class)
@Composable
private fun PhotoReactionBurstEmptyPreview() {
    PhotoReactionBurst(
        modifier = Modifier.fillMaxSize(),
        burst = null,
    )
}
