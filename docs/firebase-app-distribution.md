# Firebase App Distribution

`develop` push 시 `Firebase App Distribution` workflow가 Debug APK를 빌드하고 지정한 테스터 그룹에 배포합니다.
Actions의 `Run workflow`에서도 `develop`을 선택해 실행할 수 있습니다. 다른 브랜치에서는 배포 job을 실행하지 않습니다.
기존 PR CI는 배포 없이 린트와 Debug 빌드만 수행합니다. Play Console 배포는 별도 작업입니다.

## 고정 Debug 서명

기존 `~/.android/debug.keystore`를 CI 배포용으로 사용해도 됩니다. 같은 파일을 보관하고 매번 복원해야 합니다.
Release에 사용하는 upload keystore와는 구분합니다. Debug 키가 변경되면 이전 배포 앱 위에 업데이트할 수 없습니다.

Gradle Property 네 개를 모두 지정하면 해당 키로 Debug APK를 서명합니다. 하나라도 비어 있거나 파일이 없으면
기본 Debug 키로 대체하지 않고 실패합니다. 네 개 모두 지정하지 않으면 기존 로컬 Debug 서명을 사용합니다.

| Gradle Property            | 의미                                  |
|----------------------------|-------------------------------------|
| `challaDebugStoreFile`     | keystore의 절대 경로 또는 프로젝트 루트 기준 상대 경로 |
| `challaDebugStorePassword` | keystore 비밀번호                       |
| `challaDebugKeyAlias`      | 키 alias                             |
| `challaDebugKeyPassword`   | 키 비밀번호                              |

기본 Android Debug keystore의 alias는 `androiddebugkey`, 비밀번호는 `android`입니다.
키를 직접 생성하거나 설정을 변경했다면 해당 키의 실제 값을 사용합니다.

로컬 검증 시 네 값을 `~/.gradle/gradle.properties`에 추가하거나 `ORG_GRADLE_PROJECT_*` 환경변수로 전달합니다.
`challaDebugStoreFile`에는 `~` 대신 전체 경로를 사용합니다.

```bash
export ORG_GRADLE_PROJECT_challaDebugStoreFile="$HOME/.android/debug.keystore"
export ORG_GRADLE_PROJECT_challaDebugStorePassword=android
export ORG_GRADLE_PROJECT_challaDebugKeyAlias=androiddebugkey
export ORG_GRADLE_PROJECT_challaDebugKeyPassword=android
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

같은 키로 다시 빌드한 APK도 `adb install -r`로 설치하고 데이터 유지와 카카오 로그인을 확인합니다.
기존 앱이 다른 키로 서명되어 있으면 최초 전환 시 재설치가 필요하며, 앱 삭제는 로컬 데이터를 삭제합니다.

카카오 Developers의 Android 플랫폼 설정에 고정 키의 키 해시를 등록합니다.
다음 명령은 비밀번호를 대화형으로 입력받고 공개 인증서의 해시만 출력합니다.

```bash
keytool -exportcert -alias androiddebugkey -keystore "$HOME/.android/debug.keystore" \
  | openssl sha1 -binary | openssl base64
```

## Firebase 설정

1. `com.happyhouse.challa.debug` 앱의 App Distribution을 시작합니다.
2. 테스터 그룹을 생성하고 이메일을 등록합니다. 그룹 표시 이름이 아니라 alias를 사용합니다.
3. 해당 Firebase 프로젝트에 배포용 서비스 계정을 준비하고 `Firebase App Distribution Admin` 권한을 부여합니다.
4. 서비스 계정 JSON 키를 준비합니다. 이 파일은 `app/google-services.json`과 다르며 저장소에 커밋하지 않습니다.
5. `google-services.json`의 Debug 클라이언트와 일치하는 Firebase App ID를 확인합니다.

인증은 `GOOGLE_APPLICATION_CREDENTIALS`를 통해 서비스 계정으로 수행합니다.
설정 및
권한은 [Firebase 서비스 계정 문서](https://firebase.google.com/docs/app-distribution/authenticate-service-account?platform=android)
를 참고합니다.

## GitHub 설정

저장소 Settings → Environments에서 `firebase-distribution`을 만들고 다음 항목을 등록합니다.
배포 가능한 브랜치를 `develop`으로 제한합니다. Repository Secrets의 기존 설정도 사용할 수 있으며,
동일한 이름의 Environment Secret이 있으면 Environment 값이 사용됩니다.

### Secrets

| 이름                                     | 값                                               |
|----------------------------------------|-------------------------------------------------|
| `DEBUG_BASE_URL`                       | Debug API Base URL                              |
| `KAKAO_NATIVE_APP_KEY`                 | 카카오 네이티브 앱 키                                    |
| `GOOGLE_SERVICES_JSON_BASE64`          | Debug 클라이언트가 포함된 `google-services.json`의 base64 |
| `DEBUG_KEYSTORE_BASE64`                | 고정 Debug keystore의 base64                       |
| `DEBUG_STORE_PASSWORD`                 | 고정 keystore 비밀번호                                |
| `DEBUG_KEY_ALIAS`                      | 고정 키 alias                                      |
| `DEBUG_KEY_PASSWORD`                   | 고정 키 비밀번호                                       |
| `FIREBASE_SERVICE_ACCOUNT_JSON_BASE64` | 배포용 서비스 계정 JSON의 base64                         |

macOS에서는 다음 명령으로 keystore를 base64로 변환해 클립보드에 복사할 수 있습니다.
JSON 파일도 같은 방식으로 처리합니다. base64는 암호화가 아니므로 Secret에만 저장합니다.

```bash
base64 -i "$HOME/.android/debug.keystore" | tr -d '\n' | pbcopy
```

### Variables

| 이름                       | 값                                              |
|--------------------------|------------------------------------------------|
| `FIREBASE_DEBUG_APP_ID`  | Debug 앱의 Firebase App ID (`1:...:android:...`) |
| `FIREBASE_TESTER_GROUPS` | 테스터 그룹 alias. 여러 그룹은 쉼표로 구분                    |

## 로컬 수동 배포

고정 Debug 키로 빌드한 뒤 아래 명령을 실행합니다. 이 명령은 실제 테스터에게 APK를 배포합니다.
Firebase CLI는 workflow와 동일한 `firebase-tools@14.12.0` 버전을 사용합니다.

```bash
export GOOGLE_APPLICATION_CREDENTIALS=/absolute/path/to/firebase-service-account.json
export FIREBASE_APP_ID='1:...:android:...'
export FIREBASE_TESTER_GROUPS='team-testers'
git log -1 --format='%H%n%s' > /tmp/challa-release-notes.txt
firebase appdistribution:distribute app/build/outputs/apk/debug/app-debug.apk \
  --app "$FIREBASE_APP_ID" \
  --groups "$FIREBASE_TESTER_GROUPS" \
  --release-notes-file /tmp/challa-release-notes.txt \
  --non-interactive
```

테스터 계정에서 초대를 수락하고 설치·실행·카카오 로그인과 다음 배포의 업데이트를 확인합니다.
[Firebase CLI 배포 문서](https://firebase.google.com/docs/app-distribution/android/distribute-cli)

## Workflow와 실패 처리

필수 설정 검사 → 키와 Firebase 앱 설정 복원 → ktlint → Debug APK 빌드 → artifact 보관 → Firebase 배포 순서입니다.
진행 중 배포는 후속 push로 취소하지 않습니다. GitHub concurrency는 중간 대기 실행을 최신 실행으로 교체할 수 있으므로
모든 중간 커밋의 배포를 보장하지는 않습니다.

버전은 기존 `gradle/libs.versions.toml`의 `versionCode`와 `versionName`을 사용합니다.
커밋 SHA와 커밋 제목, workflow 실행 링크를 릴리즈 노트에 남겨 같은 버전의 빌드도 구분합니다.
버전을 변경할 때는 이미 배포한 Debug 앱의 `versionCode`보다 낮추지 않습니다.

APK와 릴리즈 노트는 Actions artifact로 14일간 보관합니다. 키와 서비스 계정 JSON은 artifact에 포함하지 않고
마지막 단계에서 삭제합니다. 업로드 실패는 workflow 실패로 표시하고 Actions summary에 안내합니다.

- 설정 누락·잘못된 서명: Secrets·Variables와 키 정보를 수정하고 실패한 실행을 재실행합니다.
- 앱 ID 불일치: `.debug` 패키지의 App ID와 `google-services.json`을 맞춥니다.
- Firebase 권한·그룹 오류: 서비스 계정 역할, App Distribution 시작 여부, 그룹 alias를 확인합니다.
- 일시적인 업로드 실패: artifact의 커밋 SHA를 확인하고 해당 실행을 재실행합니다.
- 기존 APK만 재배포: 해당 실행의 artifact를 내려받아 같은 APK와 릴리즈 노트로 수동 배포합니다.

`Run workflow`는 선택한 `develop`의 최신 커밋을 배포합니다. 과거 커밋의 재배포는 해당 실행의 재실행 또는 artifact를 사용합니다.
