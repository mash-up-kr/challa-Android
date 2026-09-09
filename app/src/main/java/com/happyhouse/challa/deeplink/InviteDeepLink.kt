package com.happyhouse.challa.deeplink

import android.content.Intent
import android.net.Uri

/** 웹 초대 페이지의 '앱에서 보기' 버튼이 여는 앱 스킴. `challa://invite/{초대코드}` */
private const val APP_SCHEME = "challa"

/** 초대 링크 웹 도메인. `https://challa.stellaris.co.kr/invite/{초대코드}` */
private const val INVITE_WEB_HOST = "challa.stellaris.co.kr"

private const val INVITE_PATH = "invite"

/**
 * 방 초대 링크로 들어온 Intent 에서 초대 코드를 꺼낸다. 초대 링크가 아니면 null 을 반환한다.
 *
 * 링크를 눌러 바로 앱이 열리는 App Link 와, 웹 초대 페이지의 '앱에서 보기' 버튼이 여는 앱 스킴을 모두 받는다.
 */
fun Intent.parseInvitationCode(): String? {
    if (action != Intent.ACTION_VIEW) return null
    val uri = data ?: return null

    val invitationCode =
        when {
            // challa://invite/{초대코드} 는 host 가 invite 라 코드가 첫 번째 path segment 에 온다.
            uri.scheme == APP_SCHEME && uri.host == INVITE_PATH -> uri.pathSegments.firstOrNull()
            // https://challa.stellaris.co.kr/invite/{초대코드}
            uri.isInviteWebLink() -> uri.pathSegments.getOrNull(1)
            else -> null
        }

    return invitationCode?.takeIf(String::isNotBlank)
}

private fun Uri.isInviteWebLink(): Boolean = host == INVITE_WEB_HOST && pathSegments.firstOrNull() == INVITE_PATH
