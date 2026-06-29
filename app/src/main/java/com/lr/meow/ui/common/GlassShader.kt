package com.lr.meow.ui.common

import org.intellij.lang.annotations.Language

@Language("AGSL")
val glassAGSL = """
    uniform shader content;
    uniform float2 barCenter;
    uniform float2 barHalfSize;
    uniform float iRadius;
    
    // 调节参数
    uniform float iRefractionFactor;
    uniform float iDispersionFactor;
    uniform float iRimBrightness;

    float sdRRect(float2 p, float2 b, float r) {
        float2 q = abs(p) - b + r;
        return min(max(q.x, q.y), 0.0) + length(max(q, float2(0.0))) - r;
    }

    half4 main(float2 coord) {
        half4 baseColor = content.eval(coord);

        float2 p = coord - barCenter;
        float d = sdRRect(p, barHalfSize, iRadius);

        // influence: 1.0 在区域内, 0.0 在外部
        float influence = 1.0 - smoothstep(-2.0, 2.0, d);
        if (influence <= 0.0) return baseColor;

        // 【关键改动】：边缘折射遮罩 (Edge Mask)
        // d 是到边缘的距离 (负数代表在内部)。从距离边缘 40px 开始出现折射，越靠近边缘折射越强。
        // 这样可以保证玻璃中央平坦区域 (d < -40) 完全没有扭曲变形。
        float edgeFactor = smoothstep(-40.0, -10.0, d);

        // 计算法线 (方向向外)
        // 恢复了真实的 SDF 物理法线。
        float eps = 1.0;
        float2 grad = float2(
            sdRRect(p + float2(eps, 0.0), barHalfSize, iRadius) - d,
            sdRRect(p + float2(0.0, eps), barHalfSize, iRadius) - d
        );
        // 【关键修复】：当像素恰好在对称轴上时 (如 p.y = -0.5)，正向差分会导致 grad=(0,0)。
        // normalize(0,0) 会返回 NaN。而 NaN * 0 依然是 NaN，导致屏幕出现一条 1px 的黑线！
        // 因此必须安全地归一化：
        float gradLen = length(grad);
        float2 dir = gradLen > 0.0001 ? grad / gradLen : float2(0.0, 1.0);

        // 折射强度与色散：仅在边缘生效
        float refraction = iRefractionFactor * edgeFactor;
        float dispersion = smoothstep(-40.0, 0.0, d) * iDispersionFactor;

        // 向内拉伸，模拟玻璃厚度折射
        float2 offsetR = dir * (refraction + dispersion * 1.5);
        float2 offsetG = dir * refraction;
        float2 offsetB = dir * (refraction - dispersion);

        half chR = content.eval(coord - offsetR).r;
        half chG = content.eval(coord - offsetG).g;
        half chB = content.eval(coord - offsetB).b;

        // 边缘高光
        float rim = pow(smoothstep(-15.0, 0.0, d), 2.5) * iRimBrightness;
        half3 glassBody = half3(chR, chG, chB) * 1.08 + half3(rim);

        return half4(mix(baseColor.rgb, glassBody, influence), baseColor.a);
    }
""".trimIndent()