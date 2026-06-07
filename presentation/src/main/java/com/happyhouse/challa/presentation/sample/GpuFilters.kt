package com.happyhouse.challa.presentation.sample

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.opengl.GLES20
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageLookupFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageToneCurveFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageVignetteFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageWhiteBalanceFilter
import java.nio.FloatBuffer
import kotlin.math.pow

// UI에 노출되는 필터 항목. label은 화면에 표시되는 이름이고,
// create는 실제 GPUImage 필터 인스턴스를 만들어주는 팩토리.
// 필터를 매번 새로 만드는 이유: GPUImageFilter는 GL 컨텍스트에 바인딩되므로 재사용 시 충돌 위험이 있음.
data class FilmFilter(val label: String, val create: () -> GPUImageFilter)

// 앱이 사용할 전체 필터 목록을 만든다.
// 코드로 박혀있는 빌트인 필터(BuiltinFilm) + assets/luts 폴더의 LUT 이미지로 만든 필터를 합쳐서 반환.
fun loadFilmFilters(context: Context): List<FilmFilter> {
    val builtin = BuiltinFilm.entries.map { FilmFilter(it.label, it::create) }
    return builtin + assetLutFilters(context)
}

// 코드에 직접 하드코딩된 빌트인 필터 프리셋들.
// 각 항목은 자기 자신을 만드는 create() 함수를 들고 있고,
// CLASSIC/BW처럼 여러 필터를 GPUImageFilterGroup으로 묶어서 체인으로 적용한다.
// 체인은 listOf 순서대로 위→아래로 적용됨 (예: 화이트밸런스 → 콘트라스트 → 톤커브 → 비네트 → 그레인).
enum class BuiltinFilm(val label: String) {
    // 원본 그대로. 아무 효과도 안 줌.
    NORMAL("Normal") {
        override fun create(): GPUImageFilter = GPUImageFilter()
    },
    // 따뜻한 색감의 클래식 필름룩.
    CLASSIC("Classic") {
        override fun create(): GPUImageFilter = GPUImageFilterGroup(
            listOf(
                // 색온도 5400K로 살짝 따뜻하게, tint 8로 마젠타 약간.
                GPUImageWhiteBalanceFilter(5400f, 8f),
                // 대비 10% 증가.
                GPUImageContrastFilter(1.1f),
                // 톤커브: 어두운 부분을 0.04로 띄우고 밝은 부분을 0.96으로 눌러서 "필름 같은" S 곡선의 약한 버전.
                GPUImageToneCurveFilter().apply {
                    setRgbCompositeControlPoints(
                        arrayOf(
                            PointF(0f, 0.04f),
                            PointF(0.5f, 0.5f),
                            PointF(1f, 0.96f),
                        )
                    )
                },
                // 비네트: 화면 중앙 기준 반경 0.4부터 0.85까지 검은색으로 페이드아웃 → 모서리 살짝 어둡게.
                GPUImageVignetteFilter(
                    PointF(0.5f, 0.5f),
                    floatArrayOf(0f, 0f, 0f),
                    0.4f,
                    0.85f,
                ),
                // 필름 그레인 노이즈. 강도 0.05는 약하게.
                GPUImageGrainFilter(strength = 0.05f),
            )
        )
    },
    // 흑백 필름룩.
    BW("B&W") {
        override fun create(): GPUImageFilter = GPUImageFilterGroup(
            listOf(
                // 우선 흑백으로 변환.
                GPUImageGrayscaleFilter(),
                // 흑백은 대비를 더 강하게 (1.2배).
                GPUImageContrastFilter(1.2f),
                // 톤커브를 4포인트로 더 정교하게 → 진한 S 커브로 흑백의 깊이감을 살림.
                GPUImageToneCurveFilter().apply {
                    setRgbCompositeControlPoints(
                        arrayOf(
                            PointF(0f, 0.02f),
                            PointF(0.3f, 0.22f),
                            PointF(0.7f, 0.78f),
                            PointF(1f, 0.98f),
                        )
                    )
                },
                GPUImageVignetteFilter(
                    PointF(0.5f, 0.5f),
                    floatArrayOf(0f, 0f, 0f),
                    0.35f,
                    0.8f,
                ),
                // 흑백은 그레인을 좀 더 강하게 (0.1) → 필름 느낌 더 살림.
                GPUImageGrainFilter(strength = 0.1f),
            )
        )
    },
    // 아래 셋은 LUT(Lookup Table) 기반 필터. 색 변환을 미리 만들어둔 표(LUT)에서 찾아서 적용.
    // 코닥 포트라 풍 - 부드럽고 살구빛 피부톤.
    PORTRA("Portra") {
        override fun create(): GPUImageFilter =
            lutFilm(LutLibrary.portra, grain = 0.04f, vignetteStart = 0.45f)
    },
    // 후지 벨비아 풍 - 채도 강하고 쨍한 색감.
    VELVIA("Velvia") {
        override fun create(): GPUImageFilter =
            lutFilm(LutLibrary.velvia, grain = 0.03f, vignetteStart = 0.5f)
    },
    // 블리치 바이패스 - 채도는 낮추고 대비를 올린 표백 처리 룩.
    BLEACH("Bleach") {
        override fun create(): GPUImageFilter =
            lutFilm(LutLibrary.bleachBypass, grain = 0.06f, vignetteStart = 0.35f)
    };

    abstract fun create(): GPUImageFilter
}

// assets/luts 폴더에 들어있는 LUT 파일들을 읽어서 필터로 만들어준다.
// 지원 포맷:
//  - PNG/JPG : GPUImage 전용 512x512 LUT 이미지. 그대로 비트맵으로 디코드해서 씀.
//  - .cube   : DaVinci/Premiere 등에서 흔히 쓰는 텍스트 LUT. 파싱 후 64³로 트라일리니어 리샘플링해서 512x512 비트맵으로 굽는다.
// 파일 이름이 그대로 필터 라벨이 됨 (kodak.cube → "Kodak").
// assets 폴더가 없거나 접근 실패해도 크래시 안 나게 try/catch로 빈 배열 반환.
private fun assetLutFilters(context: Context): List<FilmFilter> {
    val files = try {
        context.assets.list("luts") ?: emptyArray()
    } catch (_: Exception) {
        emptyArray()
    }
    return files.mapNotNull { fileName ->
        val bm = when {
            fileName.endsWith(".png", true) || fileName.endsWith(".jpg", true) ->
                context.assets.open("luts/$fileName").use { BitmapFactory.decodeStream(it) }
            fileName.endsWith(".cube", true) ->
                context.assets.open("luts/$fileName").bufferedReader().use { bakeCubeLut(parseCubeLut(it)) }
            else -> null
        } ?: return@mapNotNull null
        val label = fileName.substringBeforeLast('.').replaceFirstChar { it.uppercase() }
        FilmFilter(label) { lutFilm(bm, grain = 0.04f, vignetteStart = 0.4f) }
    }
}

// .cube 파일 파싱 결과. size는 한 축당 격자 수 (17/25/33/65 등).
// data는 R이 가장 빠르게 변하는 순서로 size³개의 RGB 트리플렛이 평탄화된 배열 (길이 size*size*size*3).
private class CubeLut(val size: Int, val data: FloatArray)

// Adobe/Resolve .cube 1.0 포맷 파서.
// 헤더(TITLE/LUT_3D_SIZE/DOMAIN_MIN/MAX)와 주석(#)을 건너뛰고, 공백으로 구분된 RGB 트리플렛만 모은다.
// DOMAIN은 거의 모든 컨슈머 LUT이 0~1이라 일단 무시 (필요해지면 정규화 단계 추가).
// 1D LUT(LUT_1D_SIZE)은 지원 안 함 — 색감용 LUT은 99% 3D.
private fun parseCubeLut(reader: java.io.BufferedReader): CubeLut {
    var size = 0
    val values = ArrayList<Float>(64 * 64 * 64 * 3)
    val ws = Regex("\\s+")
    reader.lineSequence().forEach { raw ->
        val line = raw.trim()
        if (line.isEmpty() || line.startsWith("#")) return@forEach
        when {
            line.startsWith("TITLE", true) -> Unit
            line.startsWith("DOMAIN_MIN", true) -> Unit
            line.startsWith("DOMAIN_MAX", true) -> Unit
            line.startsWith("LUT_3D_SIZE", true) -> {
                size = line.split(ws).last().toInt()
            }
            line.startsWith("LUT_1D_SIZE", true) ->
                error("1D .cube LUT은 지원하지 않습니다. 3D LUT을 사용하세요.")
            else -> {
                // 데이터 라인. 공백/탭으로 구분된 3개 float.
                val parts = line.split(ws)
                if (parts.size >= 3) {
                    values += parts[0].toFloat()
                    values += parts[1].toFloat()
                    values += parts[2].toFloat()
                }
            }
        }
    }
    require(size > 0) { "LUT_3D_SIZE를 찾을 수 없습니다." }
    val expected = size * size * size * 3
    require(values.size == expected) {
        ".cube 데이터 개수 불일치: ${values.size / 3}개, 예상 ${size * size * size}개"
    }
    return CubeLut(size, values.toFloatArray())
}

// 파싱된 N³ 큐브를 셰이더가 요구하는 64³(=512x512 PNG)로 트라일리니어 보간해서 굽는다.
// N==64면 보간 없이 그대로 복사되고, N==17/33 등이면 인접 8개 격자점을 가중평균.
// 타일 레이아웃은 buildLut과 동일 (8x8 격자, 각 64x64 셀).
private fun bakeCubeLut(cube: CubeLut): Bitmap {
    val tilesPerRow = 8
    val cellsPerEdge = 64
    val outSize = tilesPerRow * cellsPerEdge
    val pixels = IntArray(outSize * outSize)
    val outMax = (cellsPerEdge - 1).toFloat()
    val srcMax = (cube.size - 1).toFloat()
    val n = cube.size

    // .cube 데이터 인덱싱: R이 가장 빠른 축 → index = ((b*N) + g)*N + r, 채널 오프셋은 *3+c.
    fun sample(r: Int, g: Int, b: Int, c: Int): Float =
        cube.data[((b * n + g) * n + r) * 3 + c]

    for (bi in 0 until cellsPerEdge) {
        val tileX = (bi % tilesPerRow) * cellsPerEdge
        val tileY = (bi / tilesPerRow) * cellsPerEdge
        // 출력 격자점 bi를 입력 큐브 좌표계로 매핑 (0..srcMax 사이의 실수).
        val bSrc = bi / outMax * srcMax
        val b0 = bSrc.toInt().coerceIn(0, n - 1)
        val b1 = (b0 + 1).coerceAtMost(n - 1)
        val bt = bSrc - b0
        for (gi in 0 until cellsPerEdge) {
            val gSrc = gi / outMax * srcMax
            val g0 = gSrc.toInt().coerceIn(0, n - 1)
            val g1 = (g0 + 1).coerceAtMost(n - 1)
            val gt = gSrc - g0
            val rowBase = (tileY + gi) * outSize + tileX
            for (ri in 0 until cellsPerEdge) {
                val rSrc = ri / outMax * srcMax
                val r0 = rSrc.toInt().coerceIn(0, n - 1)
                val r1 = (r0 + 1).coerceAtMost(n - 1)
                val rt = rSrc - r0

                // 채널별로 8개 코너 샘플링 후 트라일리니어 보간.
                // (1-t)*A + t*B 를 r → g → b 순서로 세 번 적용.
                val outRgb = IntArray(3)
                for (c in 0..2) {
                    val c000 = sample(r0, g0, b0, c); val c100 = sample(r1, g0, b0, c)
                    val c010 = sample(r0, g1, b0, c); val c110 = sample(r1, g1, b0, c)
                    val c001 = sample(r0, g0, b1, c); val c101 = sample(r1, g0, b1, c)
                    val c011 = sample(r0, g1, b1, c); val c111 = sample(r1, g1, b1, c)
                    val c00 = c000 + (c100 - c000) * rt
                    val c10 = c010 + (c110 - c010) * rt
                    val c01 = c001 + (c101 - c001) * rt
                    val c11 = c011 + (c111 - c011) * rt
                    val c0 = c00 + (c10 - c00) * gt
                    val c1 = c01 + (c11 - c01) * gt
                    val v = c0 + (c1 - c0) * bt
                    outRgb[c] = (v.coerceIn(0f, 1f) * 255f).toInt()
                }
                pixels[rowBase + ri] =
                    (0xFF shl 24) or (outRgb[0] shl 16) or (outRgb[1] shl 8) or outRgb[2]
            }
        }
    }
    return Bitmap.createBitmap(pixels, outSize, outSize, Bitmap.Config.ARGB_8888)
}

// LUT 비트맵 하나를 받아서 "LUT 색변환 + 비네트 + 그레인" 3단 체인으로 묶어주는 헬퍼.
// vignetteStart부터 vignetteStart+0.45 구간에서 페이드아웃 → start가 작을수록 비네트가 화면 안쪽까지 들어옴.
private fun lutFilm(lut: Bitmap, grain: Float, vignetteStart: Float): GPUImageFilter =
    GPUImageFilterGroup(
        listOf(
            GPUImageLookupFilter().apply { bitmap = lut },
            GPUImageVignetteFilter(
                PointF(0.5f, 0.5f),
                floatArrayOf(0f, 0f, 0f),
                vignetteStart,
                vignetteStart + 0.45f,
            ),
            GPUImageGrainFilter(strength = grain),
        )
    )

// 코드로 생성되는 LUT 비트맵을 보관하는 캐시. by lazy로 첫 접근 시 한 번만 만들고 재사용.
// LUT 생성은 무겁기 때문에 (64^3 = 26만 픽셀 계산) 절대 매번 만들면 안 됨.
object LutLibrary {
    val portra: Bitmap by lazy { buildLut(::portraTransform) }
    val velvia: Bitmap by lazy { buildLut(::velviaTransform) }
    val bleachBypass: Bitmap by lazy { buildLut(::bleachBypassTransform) }
}

// 포트라(Portra) 톤: 채널마다 감마(거듭제곱)와 게인을 다르게 줘서
// 살구빛/따뜻한 톤을 만든다. R은 살짝 올리고, B는 살짝 누름.
private fun portraTransform(r: Float, g: Float, b: Float): FloatArray {
    val nr = ((r * 0.93f + 0.07f).pow(0.92f) * 1.04f + 0.01f)
    val ng = ((g * 0.94f + 0.05f).pow(0.95f))
    val nb = ((b * 0.91f + 0.04f).pow(0.97f) * 0.94f)
    return floatArrayOf(nr, ng, nb)
}

// 벨비아(Velvia) 톤: 채도를 1.35배 끌어올리고 S커브로 대비를 키운다.
// 휘도(lum)는 사람 눈에 맞춘 BT.601 계수 (R 30%, G 59%, B 11%) 기준.
// "lum + (channel - lum) * sat" 식은 휘도는 유지하고 색 차이만 키우는 표준 채도 공식.
private fun velviaTransform(r: Float, g: Float, b: Float): FloatArray {
    val lum = 0.299f * r + 0.587f * g + 0.114f * b
    val sat = 1.35f
    val sr = lum + (r - lum) * sat
    val sg = lum + (g - lum) * sat
    val sb = lum + (b - lum) * sat
    return floatArrayOf(sCurve(sr), sCurve(sg), sCurve(sb))
}

// 블리치 바이패스: 영화 후반 작업의 표백 처리 흉내.
// 1) 채도를 0.35로 확 낮춰 거의 흑백에 가깝게.
// 2) 그 다음 대비를 1.45배로 확 올림 (중심 0.5 기준으로 위아래로 늘림).
// 3) 파란 채널은 중심을 0.55로 살짝 올려 차가운 톤 부여.
private fun bleachBypassTransform(r: Float, g: Float, b: Float): FloatArray {
    val lum = 0.299f * r + 0.587f * g + 0.114f * b
    val sat = 0.35f
    val dr = lum + (r - lum) * sat
    val dg = lum + (g - lum) * sat
    val db = lum + (b - lum) * sat
    val cr = (dr - 0.5f) * 1.45f + 0.5f
    val cg = (dg - 0.5f) * 1.45f + 0.5f
    val cb = (db - 0.5f) * 1.45f + 0.55f
    return floatArrayOf(cr, cg, cb)
}

// smoothstep 곡선. 0~1 입력을 받아 0~1로 부드럽게 매핑.
// 양 끝(0, 1)에서 기울기가 0이라서 어두운 쪽/밝은 쪽 모두 부드럽게 마무리됨 → 자연스러운 S커브.
private fun sCurve(v: Float): Float {
    val x = v.coerceIn(0f, 1f)
    return x * x * (3f - 2f * x)
}

// 색 변환 함수(transform)를 받아서 GPUImageLookupFilter가 쓸 수 있는 LUT 비트맵으로 굽는다.
//
// LUT 구조: 64x64x64의 3D 색 큐브를 2D 이미지로 펼친 것.
// - 64개의 파란색 슬라이스(blueIndex 0~63)를 8x8 격자(tilesPerRow=8)로 배열
// - 각 슬라이스는 64x64 타일이고, 타일 안의 (x, y) 픽셀이 (R, G) 값에 해당
// - 최종 크기: (8*64) x (8*64) = 512 x 512
//
// 즉 어떤 입력 색 (R, G, B)이 들어왔을 때 GL 셰이더는
//   1) B로 어느 타일인지 결정 (8x8 격자에서 위치),
//   2) R, G로 타일 내부 어느 픽셀인지 결정해서
//   해당 픽셀 색을 출력으로 쓴다.
fun buildLut(transform: (Float, Float, Float) -> FloatArray): Bitmap {
    val tilesPerRow = 8
    val cellsPerEdge = 64
    val size = tilesPerRow * cellsPerEdge
    val pixels = IntArray(size * size)
    val maxIndex = (cellsPerEdge - 1).toFloat()

    // 3중 루프로 64^3 = 262,144개의 색 조합을 모두 계산해서 각 픽셀에 굽는다.
    for (blueIndex in 0 until cellsPerEdge) {
        // 이 B 슬라이스가 큰 이미지에서 어느 타일에 들어가는지 좌상단 좌표 계산.
        val tileX = (blueIndex % tilesPerRow) * cellsPerEdge
        val tileY = (blueIndex / tilesPerRow) * cellsPerEdge
        val b = blueIndex / maxIndex
        for (greenIndex in 0 until cellsPerEdge) {
            val g = greenIndex / maxIndex
            // 이 행의 픽셀 배열 시작 인덱스. 매번 곱셈 줄이려고 바깥에서 계산.
            val rowBase = (tileY + greenIndex) * size + tileX
            for (redIndex in 0 until cellsPerEdge) {
                val r = redIndex / maxIndex
                // 실제 색 변환 적용 (포트라/벨비아/블리치).
                val out = transform(r, g, b)
                // 0~1 float를 0~255 정수로 변환. 범위 벗어나면 잘라냄.
                val rb = (out[0].coerceIn(0f, 1f) * 255f).toInt()
                val gb = (out[1].coerceIn(0f, 1f) * 255f).toInt()
                val bb = (out[2].coerceIn(0f, 1f) * 255f).toInt()
                // ARGB 8888 픽셀 포맷: 알파(FF) | R | G | B 순서로 32비트 정수에 패킹.
                pixels[rowBase + redIndex] =
                    (0xFF shl 24) or (rb shl 16) or (gb shl 8) or bb
            }
        }
    }

    return Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
}

// 필름 그레인(노이즈) 효과를 주는 커스텀 GPU 필터.
// GPUImage 기본 제공 필터에는 그레인이 없어서 셰이더를 직접 짠 것.
// strength: 노이즈 세기 (0이면 노이즈 없음, 0.1 정도면 꽤 거칠어짐).
class GPUImageGrainFilter(private var strength: Float = 0.05f) : GPUImageFilter(
    NO_FILTER_VERTEX_SHADER,
    GRAIN_FRAGMENT_SHADER,
) {
    // 셰이더 uniform 변수의 위치(주소). onInit에서 한 번 받아두고 매 프레임 setFloat로 값 전달.
    private var strengthLocation: Int = 0
    private var seedLocation: Int = 0

    // GL 프로그램이 컴파일된 직후 호출. uniform 위치를 캐싱한다.
    override fun onInit() {
        super.onInit()
        strengthLocation = GLES20.glGetUniformLocation(program, "strength")
        seedLocation = GLES20.glGetUniformLocation(program, "seed")
    }

    // 필터 준비 완료. strength는 한 번만 세팅하면 됨 (매 프레임 안 바뀌므로).
    override fun onInitialized() {
        super.onInitialized()
        setFloat(strengthLocation, strength)
    }

    // 매 프레임 그릴 때마다 호출. seed를 매번 새로 줘서 노이즈 패턴이 움직이게 함.
    // (seed가 고정되면 노이즈가 화면에 박혀버려서 그레인 느낌이 안 남)
    override fun onDraw(textureId: Int, cubeBuffer: FloatBuffer, textureBuffer: FloatBuffer) {
        setFloat(seedLocation, Math.random().toFloat())
        super.onDraw(textureId, cubeBuffer, textureBuffer)
    }

    companion object {
        // 프래그먼트 셰이더 (GLSL). 픽셀마다 한 번씩 GPU에서 실행됨.
        //
        // rand(): 텍스처 좌표 + seed를 해시처럼 섞어서 -0.5~+0.5 사이의 의사난수 생성.
        //   sin(dot(...)) * 43758 의 소수부를 쓰는 것은 GLSL에서 흔히 쓰는 "싸구려 노이즈" 트릭.
        //   진짜 난수는 아니지만 GPU에서 빠르고 시각적으로 충분히 랜덤해 보임.
        //
        // main(): 원본 픽셀 색에 (noise * strength)를 더해서 출력.
        //   alpha는 건드리지 않음.
        const val GRAIN_FRAGMENT_SHADER =
            "varying highp vec2 textureCoordinate;\n" +
                "uniform sampler2D inputImageTexture;\n" +
                "uniform highp float strength;\n" +
                "uniform highp float seed;\n" +
                "\n" +
                "highp float rand(highp vec2 co) {\n" +
                "    return fract(sin(dot(co + seed, vec2(12.9898, 78.233))) * 43758.5453);\n" +
                "}\n" +
                "\n" +
                "void main() {\n" +
                "    lowp vec4 color = texture2D(inputImageTexture, textureCoordinate);\n" +
                "    highp float noise = rand(textureCoordinate) - 0.5;\n" +
                "    gl_FragColor = vec4(color.rgb + noise * strength, color.a);\n" +
                "}\n"
    }
}

