package com.happyhouse.challa.domain.model

/**
 * 사진에 남기는 반응 이모지.
 *
 * 서버에는 [name] 문자열 그대로 저장한다.
 * 선언 순서가 곧 반응 바에 노출되는 순서다.
 */
enum class ReactionEmoji {
    FIRE,
    EYES,
    MEDAL,
    QUESTION,
    THINKING,
    HEART,
    THUMBS_UP,
    SPARKLES,
    POOP,
    SKULL,
    ;

    companion object {
        /**
         * 모르는 값이면 null이다.
         *
         * 상태값과 달리 UNKNOWN으로 그릴 그림이 없어 화면에서 표현할 방법이 없다.
         * 새 이모지가 추가된 상위 버전에서 남긴 반응이 내려오는 경우라, 호출부에서 걸러내고 로그를 남긴다.
         */
        fun from(value: String): ReactionEmoji? = entries.find { it.name.equals(value, ignoreCase = true) }
    }
}
