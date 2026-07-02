#!/usr/bin/env python3
import os
import time
import google.generativeai as genai
from github import Github

GEMINI_API_KEY = os.environ["GEMINI_API_KEY"]
GITHUB_TOKEN = os.environ["GITHUB_TOKEN"]
REPO = os.environ["GITHUB_REPOSITORY"]
PR_NUMBER = int(os.environ["PR_NUMBER"])

MODELS = ["gemini-2.5-flash", "gemini-2.0-flash", "gemini-1.5-flash"]


def PR_변경사항(pr) -> str:
    결과 = []
    for 파일 in pr.get_files():
        헤더 = f"[{파일.status.upper()}] {파일.filename} (+{파일.additions}/-{파일.deletions})"
        패치 = 파일.patch or "(바이너리 또는 diff 없음)"
        결과.append(f"{헤더}\n{패치}")
    return "\n\n".join(결과)


def 리뷰_프롬프트(제목: str, 본문: str, diff: str) -> str:
    return f"""너는 이 Android 앱 팀의 시니어 리뷰어야. 아래 PR을 **엄격하고 날카롭게** 리뷰해.
칭찬·코드 설명·요약 나열은 필요 없고, 머지를 막아야 할 실제 결함을 찾는 게 목적이야.

PR 제목: {제목}
PR 설명: {본문 or "(없음)"}

변경사항(diff):
{diff}

리뷰할 때 이런 부분 위주로 봐줘 (우선순위 순):
- **버그**: Null 안전성, 동시성/스레드, Flow/Coroutine 오용(스코프·취소·collect 누락 등), 메모리/리소스 누수, 예외 처리 누락
- domain/data/presentation 모듈 경계 침범 여부
- Hilt, Coroutine, Flow 사용 방식
- 상태 관리(불가능한 상태 표현, 불필요한 recomposition)와 성능/UI 이슈
- 중복 코드, 불필요한 추상화, 더 단순한 표준 API로 대체 가능한 부분

작성 규칙 (반드시 지킬 것):
- **한국어**로 작성
- **Critical / High / Medium 등급만 보고**. Low·Nit·스타일 취향·단순 칭찬은 **전부 생략** (ktlint가 잡는 포맷 이슈도 생략)
- 추측·일반론 금지. **diff에 실제로 존재하는 코드**만 근거로. 확신 없으면 적지 마
- 각 문제는 `파일:라인` → 무엇이 왜 문제인지 → **구체적 수정 방향**(가능하면 코드 스니펫) 순으로
- diff에 안 보이는 부분은 단정하지 말고 "확인 필요"로만 짧게

아래 형식으로만 작성해줘:

## 📋 요약
(머지 가능 여부 판단 한 줄 + PR 핵심 2문장 이내. 막아야 할 결함이 있으면 명시)

## 🔍 발견된 문제
심각도 높은 순으로 나열. 각 항목 형식:

### 🔴 Critical / 🟠 High / 🟡 Medium — (한 줄 제목)
- **위치**: `파일:라인`
- **문제**: (무엇이 왜 문제인지)
- **수정**: (구체적 방향 / 코드)

문제가 하나도 없으면 정확히 이렇게만 써: `Critical/High/Medium 등급의 문제는 발견되지 않았습니다.`
"""


def gemini_리뷰(프롬프트: str) -> str:
    genai.configure(api_key=GEMINI_API_KEY)

    for 모델명 in MODELS:
        print(f"모델: {모델명}")
        모델 = genai.GenerativeModel(모델명)
        for 시도 in range(3):
            try:
                응답 = 모델.generate_content(
                    프롬프트,
                    generation_config=genai.types.GenerationConfig(
                        temperature=0.3,
                        max_output_tokens=8192,
                    ),
                )
                return 응답.text
            except Exception as e:
                오류 = str(e)
                if "429" in 오류 or "quota" in 오류.lower():
                    대기 = 30 * (시도 + 1)
                    print(f"  요청 한도 초과, {대기}초 후 재시도 ({시도 + 1}/3)")
                    time.sleep(대기)
                elif "503" in 오류 or "unavailable" in 오류.lower():
                    print(f"  {모델명} 사용 불가, 다음 모델로")
                    break
                else:
                    raise

    raise RuntimeError("모든 모델 시도 실패")


def main():
    print(f"PR #{PR_NUMBER} 리뷰 시작")

    g = Github(GITHUB_TOKEN)
    repo = g.get_repo(REPO)
    pr = repo.get_pull(PR_NUMBER)

    print(f"제목: {pr.title} / 변경 파일 수: {pr.changed_files}")

    diff = PR_변경사항(pr)

    if not diff.strip():
        print("변경사항 없음")
        return

    if len(diff) > 100000:
        diff = diff[:100000] + "\n\n... (너무 길어서 일부 생략)"

    프롬프트 = 리뷰_프롬프트(pr.title, pr.body, diff)
    리뷰 = gemini_리뷰(프롬프트)

    코멘트 = (
        "## 🤖 AI 코드 리뷰 (Gemini)\n\n"
        + 리뷰
        + "\n\n---\n*자동 생성된 리뷰입니다. 판단은 작성자에게 맡깁니다.*"
    )

    pr.create_issue_comment(코멘트)
    print("리뷰 완료")


if __name__ == "__main__":
    main()
