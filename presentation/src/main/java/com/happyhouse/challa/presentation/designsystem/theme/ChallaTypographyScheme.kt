package com.happyhouse.challa.presentation.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.happyhouse.challa.presentation.designsystem.foundation.typography.ChallaTextStyles
import com.happyhouse.challa.presentation.designsystem.foundation.typography.ChallaTypography
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal object ChallaTypographyScheme {
    @Composable
    fun default() =
        ChallaTypography(
            family1 = ChallaTextStyles.Family1.toTextStyle(),
            family2 = ChallaTextStyles.Family2.toTextStyle(),
            weightBold = ChallaTextStyles.WeightBold.toTextStyle(),
            weightMedium = ChallaTextStyles.WeightMedium.toTextStyle(),
            weightRegular = ChallaTextStyles.WeightRegular.toTextStyle(),
            headingXLarge = ChallaTextStyles.HeadingXLarge.toTextStyle(),
            headingLarge = ChallaTextStyles.HeadingLarge.toTextStyle(),
            headingMedium = ChallaTextStyles.HeadingMedium.toTextStyle(),
            headingSmall = ChallaTextStyles.HeadingSmall.toTextStyle(),
            bodyLarge = ChallaTextStyles.BodyLarge.toTextStyle(),
            bodyMedium = ChallaTextStyles.BodyMedium.toTextStyle(),
            bodySmall = ChallaTextStyles.BodySmall.toTextStyle(),
            descriptionLarge = ChallaTextStyles.DescriptionLarge.toTextStyle(),
            descriptionMedium = ChallaTextStyles.DescriptionMedium.toTextStyle(),
            descriptionSmall = ChallaTextStyles.DescriptionSmall.toTextStyle(),
        )
}

@Preview(widthDp = 800, heightDp = 240)
@Composable
private fun ChallaTypographyFamilyPreview() {
    ChallaTheme {
        ChallaTypographyPreviewContent(
            title = "Family",
            items =
                persistentListOf(
                    "Family 1" to ChallaTheme.typography.family1,
                    "Family 2" to ChallaTheme.typography.family2,
                ),
        )
    }
}

@Preview(widthDp = 800, heightDp = 200)
@Composable
private fun ChallaTypographyWeightPreview() {
    ChallaTheme {
        ChallaTypographyPreviewContent(
            title = "Weight",
            items =
                persistentListOf(
                    "Weight Bold" to ChallaTheme.typography.weightBold,
                    "Weight Medium" to ChallaTheme.typography.weightMedium,
                    "Weight Regular" to ChallaTheme.typography.weightRegular,
                ),
        )
    }
}

@Preview(widthDp = 960, heightDp = 320)
@Composable
private fun ChallaTypographyHeadingPreview() {
    ChallaTheme {
        ChallaTypographyPreviewContent(
            title = "Heading",
            items =
                persistentListOf(
                    "Heading XLarge" to ChallaTheme.typography.headingXLarge,
                    "Heading Large" to ChallaTheme.typography.headingLarge,
                    "Heading Medium" to ChallaTheme.typography.headingMedium,
                    "Heading Small" to ChallaTheme.typography.headingSmall,
                ),
        )
    }
}

@Preview(widthDp = 800, heightDp = 200)
@Composable
private fun ChallaTypographyBodyPreview() {
    ChallaTheme {
        ChallaTypographyPreviewContent(
            title = "Body",
            items =
                persistentListOf(
                    "Body Large" to ChallaTheme.typography.bodyLarge,
                    "Body Medium" to ChallaTheme.typography.bodyMedium,
                    "Body Small" to ChallaTheme.typography.bodySmall,
                ),
        )
    }
}

@Preview(widthDp = 800, heightDp = 200)
@Composable
private fun ChallaTypographyDescriptionPreview() {
    ChallaTheme {
        ChallaTypographyPreviewContent(
            title = "Description",
            items =
                persistentListOf(
                    "Description Large" to ChallaTheme.typography.descriptionLarge,
                    "Description Medium" to ChallaTheme.typography.descriptionMedium,
                    "Description Small" to ChallaTheme.typography.descriptionSmall,
                ),
        )
    }
}

@Composable
private fun ChallaTypographyPreviewContent(
    title: String,
    items: ImmutableList<Pair<String, TextStyle>>,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(ChallaTheme.colors.backgroundSurface)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = title,
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.bodyLarge,
        )
        HorizontalDivider(color = ChallaTheme.colors.lineNormal)
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items.forEach { (name, style) ->
                ChallaTypographyPreviewRow(
                    name = name,
                    style = style,
                )
            }
        }
    }
}

@Composable
private fun ChallaTypographyPreviewRow(
    name: String,
    style: TextStyle,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.width(180.dp),
            text = name,
            color = ChallaTheme.colors.labelNormal,
            style = ChallaTheme.typography.descriptionLarge,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = "challa 해피하우스 화이팅",
            color = ChallaTheme.colors.labelNormal,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}
