package com.happyhouse.challa.domain.model

/** 서버에는 [name] 문자열 그대로 저장한다. */
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
        /** 상태값과 달리 UNKNOWN으로 그릴 그림이 없어, 모르는 값은 호출부에서 걸러낸다. */
        fun from(value: String): ReactionEmoji? = entries.firstOrNull { it.name == value }
    }
}
