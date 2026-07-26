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
    val Default =
        ChallaTypography(
            family1 = ChallaTextStyles.Family1.toTextStyleSet(),
            family2 = ChallaTextStyles.Family2.toTextStyleSet(),
            headingHome = ChallaTextStyles.HeadingHome.toTextStyleSet(),
            headingXLarge = ChallaTextStyles.HeadingXLarge.toTextStyleSet(),
            headingLarge = ChallaTextStyles.HeadingLarge.toTextStyleSet(),
            headingMedium = ChallaTextStyles.HeadingMedium.toTextStyleSet(),
            headingSmall = ChallaTextStyles.HeadingSmall.toTextStyleSet(),
            headingXSmall = ChallaTextStyles.HeadingXSmall.toTextStyleSet(),
            bodyLarge = ChallaTextStyles.BodyLarge.toTextStyleSet(),
            bodyMedium = ChallaTextStyles.BodyMedium.toTextStyleSet(),
            bodySmall = ChallaTextStyles.BodySmall.toTextStyleSet(),
            bodyXSmall = ChallaTextStyles.BodyXSmall.toTextStyleSet(),
            descriptionLarge = ChallaTextStyles.DescriptionLarge.toTextStyleSet(),
            descriptionMedium = ChallaTextStyles.DescriptionMedium.toTextStyleSet(),
            descriptionSmall = ChallaTextStyles.DescriptionSmall.toTextStyleSet(),
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
                    "Family 1" to ChallaTheme.typography.family1.regular,
                    "Family 2" to ChallaTheme.typography.family2.regular,
                ),
        )
    }
}

@Preview(widthDp = 800, heightDp = 180)
@Composable
private fun ChallaTypographyWeightPreview() {
    ChallaTheme {
        ChallaTypographyPreviewContent(
            title = "Weight",
            items =
                persistentListOf(
                    "Weight Bold" to ChallaTheme.typography.bodySmall.bold,
                    "Weight Medium" to ChallaTheme.typography.bodySmall.medium,
                    "Weight Regular" to ChallaTheme.typography.bodySmall.regular,
                ),
        )
    }
}

@Preview(widthDp = 960, heightDp = 420)
@Composable
private fun ChallaTypographyHeadingPreview() {
    ChallaTheme {
        ChallaTypographyPreviewContent(
            title = "Heading",
            items =
                persistentListOf(
                    "Heading Home" to ChallaTheme.typography.headingHome.regular,
                    "Heading XLarge" to ChallaTheme.typography.headingXLarge.regular,
                    "Heading Large" to ChallaTheme.typography.headingLarge.bold,
                    "Heading Medium" to ChallaTheme.typography.headingMedium.bold,
                    "Heading Small" to ChallaTheme.typography.headingSmall.bold,
                    "Heading XSmall" to ChallaTheme.typography.headingXSmall.bold,
                ),
        )
    }
}

@Preview(widthDp = 800, heightDp = 220)
@Composable
private fun ChallaTypographyBodyPreview() {
    ChallaTheme {
        ChallaTypographyPreviewContent(
            title = "Body",
            items =
                persistentListOf(
                    "Body Large" to ChallaTheme.typography.bodyLarge.bold,
                    "Body Medium" to ChallaTheme.typography.bodyMedium.bold,
                    "Body Small" to ChallaTheme.typography.bodySmall.bold,
                    "Body XSmall" to ChallaTheme.typography.bodyXSmall.bold,
                ),
        )
    }
}

@Preview(widthDp = 800, heightDp = 160)
@Composable
private fun ChallaTypographyDescriptionPreview() {
    ChallaTheme {
        ChallaTypographyPreviewContent(
            title = "Description",
            items =
                persistentListOf(
                    "Description Large" to ChallaTheme.typography.descriptionLarge.bold,
                    "Description Medium" to ChallaTheme.typography.descriptionMedium.bold,
                    "Description Small" to ChallaTheme.typography.descriptionSmall.bold,
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
            style = ChallaTheme.typography.bodyLarge.bold,
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
            style = ChallaTheme.typography.descriptionLarge.bold,
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
