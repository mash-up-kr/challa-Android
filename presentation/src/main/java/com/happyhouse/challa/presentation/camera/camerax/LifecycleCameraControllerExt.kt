package com.happyhouse.challa.presentation.camera.camerax

import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Controller의 콜백 기반 촬영 API를 취소 가능한 코루틴으로 변환합니다.
 *
 * 촬영 성공 시 반환되는 [ImageProxy]의 소유권은 호출자에게 이전되므로 호출자가 반드시 닫아야 합니다.
 * 코루틴이 취소된 뒤 촬영 결과가 도착하면 이 함수가 [ImageProxy]를 즉시 닫습니다.
 *
 * @param executor CameraX 촬영 콜백을 실행할 Executor
 * @param onCaptureStarted 카메라가 촬영할 프레임의 노출을 시작했을 때 호출되는 콜백
 * @return CameraX가 캡처한 이미지
 * @throws ImageCaptureException CameraX가 이미지를 캡처하지 못한 경우
 */
internal suspend fun LifecycleCameraController.takePicture(
    executor: Executor,
    onCaptureStarted: () -> Unit,
): ImageProxy =
    suspendCancellableCoroutine { continuation ->
        lateinit var delegatingCallback: DelegatingImageCapturedCallback
        delegatingCallback =
            DelegatingImageCapturedCallback(
                delegate =
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureStarted() {
                            onCaptureStarted()
                        }

                        override fun onCaptureSuccess(image: ImageProxy) {
                            delegatingCallback.dispose()
                            continuation.resume(image) { _, imageToClose, _ ->
                                imageToClose.close()
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            delegatingCallback.dispose()
                            continuation.resumeWithException(exception)
                        }
                    },
            )

        continuation.invokeOnCancellation { delegatingCallback.dispose() }
        takePicture(executor, delegatingCallback)
    }

private class DelegatingImageCapturedCallback(
    delegate: ImageCapture.OnImageCapturedCallback,
) : ImageCapture.OnImageCapturedCallback() {
    private val delegate = AtomicReference(delegate)

    fun dispose() {
        delegate.set(null)
    }

    override fun onCaptureStarted() {
        delegate.get()?.onCaptureStarted()
    }

    override fun onCaptureSuccess(image: ImageProxy) {
        delegate.get()?.onCaptureSuccess(image) ?: image.close()
    }

    override fun onError(exception: ImageCaptureException) {
        delegate.get()?.onError(exception)
    }
}
