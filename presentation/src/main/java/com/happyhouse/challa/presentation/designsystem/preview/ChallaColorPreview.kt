package com.happyhouse.challa.presentation.designsystem.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.theme.ChallaTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Preview(showBackground = true)
@Composable
private fun ChallaColorPreview() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(ChallaTheme.colors.backgroundSurface)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
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
                    "Normal" to ChallaTheme.colors.labelNormal,
                    "Strong" to ChallaTheme.colors.labelStrong,
                    "Neutral" to ChallaTheme.colors.labelNeutral,
                    "Alternative" to ChallaTheme.colors.labelAlternative,
                    "Disable" to ChallaTheme.colors.labelDisable,
                ),
        )
        ColorGroup(
            title = "Background",
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

@Composable
private fun ColorGroup(
    title: String,
    colors: ImmutableList<Pair<String, Color>>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = title,
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.bodyMedium,
        )
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
        modifier = Modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(56.dp)
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
            style = ChallaTheme.typography.descriptionLarge,
        )
    }
}
