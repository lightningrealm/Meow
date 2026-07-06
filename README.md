🎵 Meow
一款基于 Jetpack Compose 构建的 Material Design 风格音乐播放器

https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg?logo=kotlin
https://img.shields.io/badge/Jetpack%2520Compose-1.7.5-4285F4.svg?logo=android
https://img.shields.io/badge/License-MIT-blue.svg

📱 项目简介
Meow 是一款功能完整的第三方网易云音乐客户端，基于现代 Android 技术栈构建。项目采用了 MVI 架构 与 单向数据流 设计模式，结合 Jetpack Compose 实现声明式 UI，提供了流畅、直观的音乐体验。

✨ 核心功能
🔐 用户登录 —— 支持网易云音乐账号密码登录

📋 歌单管理 —— 浏览、收藏、创建个性化歌单

🔍 全局搜索 —— 快速查找歌曲、歌手、专辑

🎵 音乐播放 —— 完整的播放控制、进度拖拽、播放模式切换

🎨 主题切换 —— 支持深色/浅色模式自适应

📱 多栈导航 —— 底部 Tab 独立返回栈，切换不丢失状态

技术栈一览
层级	技术选型	说明
UI 框架	Jetpack Compose	声明式 UI，高效构建界面
导航	Navigation Compose 3	多返回栈管理，类型安全传参
架构模式	MVI + UDF	单向数据流，状态可预测
异步处理	Kotlin Coroutines + Flow	响应式数据流管理
网络请求	Retrofit + OkHttp	RESTful API 调用
依赖注入	Koin	轻量级 DI，无代码生成
图片加载	Coil	专为 Compose 优化的图片库
音频播放	Media3 (ExoPlayer)	后台播放 · 媒体会话管理
视觉特效	AGSL (Android Graphics Shading Language)	毛玻璃 · 物理倒角 · 色散效果
多模块	Gradle 模块化	按功能拆分为独立模块
🎨 视觉特效
Meow 在 UI 细节上使用了 AGSL (Android Graphics Shading Language) 实现高级视觉特效：

物理毛玻璃效果 —— 基于 SDF 的圆角矩形，边缘折射与色散模拟真实玻璃质感

动态边缘光 —— 菲涅尔效应增强立体感

安全归一化 —— 彻底消除 1px 黑线渲染异常

📁 项目结构
text
Meow/
├── app/                          # 主应用模块
├── core/                         # 核心基础模块
│   ├── common/                   # 通用工具
│   ├── network/                  # 网络层封装
│   ├── database/                 # 本地存储
│   └── ui/                       # 通用 UI 组件
├── feature/                      # 功能模块
│   ├── login/                    # 登录功能
│   ├── home/                     # 首页歌单
│   ├── search/                   # 全局搜索
│   ├── player/                   # 音乐播放器
│   └── profile/                  # 个人中心
└── build.gradle.kts              # 项目级构建配置
🚀 快速开始
环境要求
Android Studio Hedgehog | 2023.1.1+

JDK 17+

Android SDK 33+

构建与运行
bash
# 1. 克隆项目
git clone git@github.com:lightningrealm/Meow.git

# 2. 打开项目
cd Meow

# 3. 同步依赖
./gradlew build

# 4. 安装运行
./gradlew installDebug
📦 核心依赖
kotlin
// 核心框架
implementation("androidx.compose.ui:ui:1.7.5")
implementation("androidx.navigation:navigation-compose:2.8.4")

// MVI + 协程
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

// 网络
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// 依赖注入
implementation("io.insert-koin:koin-androidx-compose:4.0.0")

// 图片
implementation("io.coil-kt.coil3:coil-compose:3.0.4")

// 音频
implementation("androidx.media3:media3-exoplayer:1.5.1")
implementation("androidx.media3:media3-session:1.5.1")

// 视觉特效 (AGSL)
implementation("androidx.graphics:graphics-shapes:1.0.0")
📄 License
本项目仅供学习交流使用，请勿用于商业用途。

🙏 致谢
NeteaseCloudMusicApi —— 网易云音乐 API


👤 作者
lightningrealm

GitHub: @lightningrealm

如果你觉得这个项目不错，欢迎 ⭐ Star 支持一下！

