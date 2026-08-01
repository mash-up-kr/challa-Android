package com.happyhouse.challa.domain.model

/**
 * 앱 전반의 강조 UI에 적용되는 primary color 종류입니다.
 *
 * Compose의 `Color`와 분리된 domain model이며,
 * 실제 색상 토큰 매핑은 presentation layer에서 담당합니다.
 */
enum class PrimaryTheme {
    LEMONADE,
    RASPBERRY,
    ORANGE,
    CIDER,
    BLUEBERRY,
    ACAI_BOWL,
}
