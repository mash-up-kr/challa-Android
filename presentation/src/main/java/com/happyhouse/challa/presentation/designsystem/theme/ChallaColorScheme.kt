package com.happyhouse.challa.presentation.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.foundation.color.ChallaColors
import com.happyhouse.challa.presentation.designsystem.foundation.color.ColorTokens
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal object ChallaColorScheme {
    val Dark =
        ChallaColors(
            primaryPink = ColorTokens.PrimaryPink,
            primaryOrange = ColorTokens.PrimaryOrange,
            primaryYellow = ColorTokens.PrimaryYellow,
            primarySky = ColorTokens.PrimarySky,
            primaryBlue = ColorTokens.PrimaryBlue,
            primaryPurple = ColorTokens.PrimaryPurple,
            labelStrong = ColorTokens.LabelStrong,
            labelNormal = ColorTokens.LabelNormal,
            labelSubtle = ColorTokens.LabelSubtle,
            labelNeutral = ColorTokens.LabelNeutral,
            labelAlternative = ColorTokens.LabelAlternative,
            labelDisable = ColorTokens.LabelDisable,
            backgroundSurface = ColorTokens.BackgroundSurface,
            backgroundLevel1 = ColorTokens.BackgroundLevel1,
            backgroundLevel2 = ColorTokens.BackgroundLevel2,
            backgroundLevel3 = ColorTokens.BackgroundLevel3,
            backgroundLevel4 = ColorTokens.BackgroundLevel4,
            statusPositive = ColorTokens.StatusPositive,
            statusCautionary = ColorTokens.StatusCautionary,
            statusDestructive = ColorTokens.StatusDestructive,
            lineNormal = ColorTokens.LineNormal,
            lineNeutral = ColorTokens.LineNeutral,
            lineAlternative = ColorTokens.LineAlternative,
            staticWhite = ColorTokens.StaticWhite,
            staticBlack = ColorTokens.StaticBlack,
            materialDimmer = ColorTokens.MaterialDimmer,
        )

    val Light =
        Dark.copy(
            labelNormal = ColorTokens.StaticBlack,
            labelStrong = ColorTokens.StaticBlack,
            backgroundSurface = ColorTokens.StaticWhite,
            backgroundLevel1 = ColorTokens.StaticWhite,
            backgroundLevel2 = ColorTokens.BackgroundLevel2,
            backgroundLevel3 = ColorTokens.BackgroundLevel3,
            backgroundLevel4 = ColorTokens.BackgroundLevel4,
        )
}

@Preview(widthDp = 600, heightDp = 960, showBackground = true)
@Composable
private fun ChallaColorPreview() {
    ChallaTheme(isDarkTheme = true) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(ChallaTheme.colors.backgroundSurface)
                    .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            ColorGroup(
                title = "Primary",
                colors =
                    persistentListOf(
                        "Pink" to ChallaTheme.colors.primaryPink,
                        "Orange" to ChallaTheme.colors.primaryOrange,
                        "Yellow" to ChallaTheme.colors.primaryYellow,
                        "Sky" to ChallaTheme.colors.primarySky,
                        "Blue" to ChallaTheme.colors.primaryBlue,
                        "Purple" to ChallaTheme.colors.primaryPurple,
                    ),
            )
            ColorGroup(
                title = "Label",
                colors =
                    persistentListOf(
                        "Strong" to ChallaTheme.colors.labelStrong,
                        "Normal" to ChallaTheme.colors.labelNormal,
                        "Subtle" to ChallaTheme.colors.labelSubtle,
                        "Neutral" to ChallaTheme.colors.labelNeutral,
                        "Alternative" to ChallaTheme.colors.labelAlternative,
                        "Disable" to ChallaTheme.colors.labelDisable,
                    ),
            )
            ColorGroup(
                title = "Background",
                subtitle = "Normal",
                colors =
                    persistentListOf(
                        "Surface" to ChallaTheme.colors.backgroundSurface,
                        "Level 1" to ChallaTheme.colors.backgroundLevel1,
                        "Level 2" to ChallaTheme.colors.backgroundLevel2,
                        "Level 3" to ChallaTheme.colors.backgroundLevel3,
                        "Level 4" to ChallaTheme.colors.backgroundLevel4,
                    ),
            )
            ColorGroup(
                title = "Status",
                colors =
                    persistentListOf(
                        "Positive" to ChallaTheme.colors.statusPositive,
                        "Cautionary" to ChallaTheme.colors.statusCautionary,
                        "Destructive" to ChallaTheme.colors.statusDestructive,
                    ),
            )
            ColorGroup(
                title = "Line",
                colors =
                    persistentListOf(
                        "Normal" to ChallaTheme.colors.lineNormal,
                        "Neutral" to ChallaTheme.colors.lineNeutral,
                        "Alternative" to ChallaTheme.colors.lineAlternative,
                    ),
            )
            ColorGroup(
                title = "Static",
                colors =
                    persistentListOf(
                        "White" to ChallaTheme.colors.staticWhite,
                        "Black" to ChallaTheme.colors.staticBlack,
                    ),
            )
            ColorGroup(
                title = "Material",
                colors =
                    persistentListOf(
                        "Dimmer" to ChallaTheme.colors.materialDimmer,
                    ),
            )
        }
    }
}

@Composable
private fun ColorGroup(
    title: String,
    subtitle: String? = null,
    colors: ImmutableList<Pair<String, Color>>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text(
            text = title,
            color = ChallaTheme.colors.labelStrong,
            style = ChallaTheme.typography.bodyMedium.bold,
        )
        subtitle?.let {
            Text(
                text = it,
                color = ChallaTheme.colors.labelStrong,
                style = ChallaTheme.typography.descriptionLarge.bold,
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            colors.forEach { (name, color) ->
                ColorSwatch(
                    name = name,
                    color = color,
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    name: String,
    color: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
                    .border(
                        width = 1.dp,
                        color = ChallaTheme.colors.lineNormal,
                        shape = RoundedCornerShape(12.dp),
                    ),
        )
        Text(
            text = name,
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.descriptionLarge.bold,
        )
    }
}
