package com.lr.glassui

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onLayoutRectChanged
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.get
import androidx.core.graphics.withSave
import com.lr.glassui.model.GlassEnvironment
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.milliseconds

/**
 * 这个BlurState是自定义的，
 * 只能在android平台用，因为renderNode只在android平台上有
 * **/
class BlurState {
    var barRect by mutableStateOf(IntRect.Zero)
    val renderNode = RenderNode("BackgroundNode")
}

/**
 * 这里对应上方的BlurState写法
 * **/
fun Modifier.captureBackground(
    state: BlurState
): Modifier =
    this.drawWithContent {
        // 1. 正常画到屏幕上
        drawContent()

        // 2. 截取一份画到 RenderNode
        val width = size.width.toInt()
        val height = size.height.toInt()
        if (width > 0 && height > 0) {
            state.renderNode.setPosition(0, 0, width, height)
            val recordingCanvas = state.renderNode.beginRecording()

            // 包装 Android Native Canvas 为 Compose Canvas
            val composeCanvas = Canvas(recordingCanvas)

            // 替换 DrawContext 的 canvas
            val previousCanvas = drawContext.canvas
            drawContext.canvas = composeCanvas

            // 再次调用 drawContent，这次内容会绘制到 RenderNode 里面
            drawContent()

            // 还原 canvas
            drawContext.canvas = previousCanvas

            state.renderNode.endRecording()
        }
    }

/**
 * 这里对应跨端的写法
 * 注意：.background() modifier 只画到屏幕画布，不会进入 layer.record{}
 * 所以必须在 layer.record 里手动 drawRect 将背景色先覆盖进去
 * 否则内容为空的区域（如页面空白底部）在 layer 里是透明黑色，导致取色失败
 * **/
fun Modifier.captureBackground(
    layer: GraphicsLayer
): Modifier = composed {
    val backgroundColor = MaterialTheme.colorScheme.background
    drawWithContent {
        layer.record {
            // 先推入实体背景色，保证空白区域不会是透明黑色
            drawRect(backgroundColor)
            this@drawWithContent.drawContent()
        }
        drawContent()
    }
}

/**
 * 这里对应BlurState的写法
 * **/
fun Modifier.glassBlurBackground(
    blurState: BlurState,
    blurRadius: Float
): Modifier = this
    .onLayoutRectChanged(debounceMillis = 0){
        blurState.barRect = it.boundsInRoot
    }
    .drawBehind{
        if(blurState.barRect.isEmpty||blurState.renderNode.width==0){
            return@drawBehind
        }
        drawContext.canvas.nativeCanvas.apply {
            withSave {
                translate(-blurState.barRect.left.toFloat(), -blurState.barRect.top.toFloat())
                blurState.renderNode.setRenderEffect(
                    RenderEffect.createBlurEffect(
                        blurRadius,
                        blurRadius,
                        Shader.TileMode.CLAMP
                    )
                )
                drawRenderNode(blurState.renderNode)
            }
        }
    }
/**
 * 这里对应跨端的写法
 * **/
fun Modifier.glassBlurBackground(
    layer: GraphicsLayer,
    blurRadius: Float,
    cornerRadiusPx: Float = 80f,  // 对应你 32.dp 的圆角
    refractionFactor: Float = 30f,
    dispersionFactor: Float = 5f,
    rimBrightness: Float = 0.15f,
    onDarkBackground: (GlassEnvironment) -> Unit = {}
): Modifier = composed {
    var barRect by remember { mutableStateOf(IntRect.Zero) }
    val effectLayer = rememberGraphicsLayer()
    
    LaunchedEffect(Unit) {
        while (isActive) {
            if (!barRect.isEmpty && layer.size.width > 0) {
                try {
                    // 神级 API：把 GPU 中的图像异步提取回 CPU
                    val bitmap = layer.toImageBitmap()
                    val androidBmp = bitmap.asAndroidBitmap()
                    
                    // layer.toImageBitmap() 的坐标系 = 截图时 NavDisplay 的本地坐标系
                    // barRect 是底栏在全屏中的坐标，但 layer bitmap 的 Y 坐标从 NavDisplay 顶部开始
                    // 所以采样时直接用 barRect.center 的 x/y 即可，因为 NavDisplay 从(0,0)开始绘制
                    // 但实际上：layer 的高度 = NavDisplay.height (不含底栏)
                    // 底栏在 NavDisplay 之上（z方向），barRect 是其在屏幕上的位置
                    // 因此采样坐标应该是 barRect 相对 NavDisplay 的位置，即直接用 barRect.center
                    val bmpW = androidBmp.width
                    val bmpH = androidBmp.height
                    val centerX = barRect.center.x.coerceIn(0, bmpW - 1)
                    // 现在 layer 里有实体背景色了，可以在底栏中心直接采样
                    // 这个点对应的是底栏后面的真实内容（卡片或背景色）
                    val sampleY = barRect.center.y.coerceIn(0, bmpH - 1)

                    //Log.d("BlurState", "BitmapSize=${bmpW}x${bmpH}, BarRect=${barRect}, SampleAt=($centerX,$sampleY)")
                    
                    // 注意：toImageBitmap 返回的是 GPU 硬件位图 (Config.HARDWARE)
                    // 不能直接调用 getPixel()，必须先 Copy 一份到 CPU 内存中
                    val softwareBmp = androidBmp.copy(Bitmap.Config.ARGB_8888, false)
                    
                    val pixelColor = softwareBmp[centerX, sampleY]
                    val r = Color.red(pixelColor)
                    val g = Color.green(pixelColor)
                    val b = Color.blue(pixelColor)
                    val luminance = (0.299 * r + 0.587 * g + 0.114 * b)
                    
                    // 用完立即回收，防止每秒复制产生大量 GC 卡顿
                    softwareBmp.recycle()
                    
                    //Log.d("BlurState", "Center($centerX, $sampleY), RGB($r,$g,$b), Luminance: $luminance")
                    
                    // 回传是否为暗色背景
                    onDarkBackground(
                        GlassEnvironment(
                            dominantColor = androidx.compose.ui.graphics.Color(pixelColor),
                            luminance = luminance,
                            isDark = luminance < 128
                        )
                    )
                } catch (e: Exception) {
                    Log.e("BlurState", "Capture failed: ${e.message}")
                    // 图层可能尚未准备好
                }
            }
            delay(200.milliseconds)
        }
    }

    this.onLayoutRectChanged(throttleMillis = 0, debounceMillis = 0) { bounds ->
        barRect = bounds.boundsInRoot
    }
    .drawBehind{
        if(barRect.isEmpty||layer.size.width==0){
            return@drawBehind
        }
        val refractionChain = buildRefractionChain(
            blurRadius = blurRadius,
            barRect = barRect,
            cornerRadiusPx = cornerRadiusPx,  // 对应你 32.dp 的圆角
            refractionFactor = refractionFactor,
            dispersionFactor = dispersionFactor,
            rimBrightness = rimBrightness
        )
        
        effectLayer.renderEffect = refractionChain.asComposeRenderEffect()
        val intSize = IntSize(size.width.toInt(), size.height.toInt())
        effectLayer.record(size = intSize) {
            translate(
                left = -barRect.left.toFloat(),
                top = -barRect.top.toFloat()
            ){
                drawLayer(layer)
            }
        }
        drawLayer(effectLayer)
    }
}

// 全局复用编译好的 Shader，避免在每一帧 (120fps) 重复编译，否则会导致毁灭性的掉帧！
private val cachedGlassShader by lazy { RuntimeShader(glassAGSL) }

private fun buildRefractionChain(
    blurRadius: Float,
    barRect: IntRect,
    cornerRadiusPx: Float,
    refractionFactor: Float = 30f,
    dispersionFactor: Float = 5f,
    rimBrightness: Float = 0.15f
): RenderEffect {
    val halfWidth = barRect.width / 2f
    val halfHeight = barRect.height / 2f
    val shader = cachedGlassShader
    shader.setFloatUniform("barCenter", halfWidth, halfHeight)
    shader.setFloatUniform("barHalfSize", halfWidth, halfHeight)
    shader.setFloatUniform("iRadius", cornerRadiusPx)
    shader.setFloatUniform("iRefractionFactor", refractionFactor)
    shader.setFloatUniform("iDispersionFactor", dispersionFactor)
    shader.setFloatUniform("iRimBrightness", rimBrightness)

    val blurFx = RenderEffect.createBlurEffect(
        blurRadius, blurRadius, Shader.TileMode.CLAMP
    )
    val refractionFx = RenderEffect
        .createRuntimeShaderEffect(shader, "content") // "content" 对应 AGSL 里的 uniform shader content

    // inner=blur 先跑，outer=refraction 读已模糊的纹理
    return RenderEffect.createChainEffect(refractionFx, blurFx)
}
