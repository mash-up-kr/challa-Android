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
    return f"""너는 Android 앱 개발에 익숙한 코드 리뷰어야. 아래 PR을 보고 리뷰해줘.

PR 제목: {제목}
PR 설명: {본문 or "(없음)"}

변경사항:
{diff}

리뷰할 때 이런 부분 위주로 봐줘:
- Kotlin 코드 스타일이나 관용구 문제
- Null 안전성, 메모리 누수, 스레드 문제 같은 잠재적 버그
- domain/data/presentation 모듈 경계 침범 여부
- Hilt, Coroutine, Flow 사용 방식
- 성능이나 UI 관련 이슈

주의사항:
- 한국어로 작성해줘
- 실제로 문제가 있는 것만 지적해 (코드가 뭘 하는지 설명하는 건 필요 없어)
- 심각도는 🔴 Critical / 🟠 High / 🟡 Medium / 🟢 Low 로 표시해줘

아래 형식으로 작성해줘:

## 📋 리뷰 요약
(PR 목적과 코드 품질 전반 2~3문장)

## 🔍 발견된 문제
(없으면 "특별한 문제 없음"이라고 써줘)

## ✅ 잘된 점
(긍정적인 부분)
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
