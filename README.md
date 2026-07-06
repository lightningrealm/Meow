# 🎵 Meow

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)](https://kotlinlang.org) 
[![Jetpack Compose](https://img.shields.io/badge/Compose-1.7+-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

一款基于 **Jetpack Compose** 构建的 **Material Design 3** 风格现代音乐播放器。

---

## 📱 项目简介

**Meow** 是一款功能完整的第三方网易云音乐客户端，完全基于现代 Android 技术栈构建。项目采用 **MVI (Model-View-Intent) 架构** 与 **UDF (单向数据流)** 设计模式，结合 Jetpack Compose 实现全声明式 UI。在保障高能效音频播放的同时，极致追求细腻的动效与前沿的视觉特效，致力于提供流畅、直观且优雅的音乐社交与视听体验。

---

## ✨ 核心功能

* 🔐 **用户登录** —— 支持网易云音乐账号密码、扫码登录及安全凭证登录。
* 📋 **歌单管理** —— 动态浏览、快捷收藏、智能创建个性化歌单与每日推荐。
* 🔍 **全局搜索** —— 联想输入，快速查找全网歌曲、歌手、精选专辑。
* 🎵 **音乐播放** —— 完备的后台播放控制、动态进度拖拽、多样化播放模式（顺序/随机/单曲循环）切换。
* 🎨 **动态主题** —— 深度支持深色/浅色（Dark/Light Mode）模式智能自适应。
* 📱 **多栈导航** —— 基于独立返回栈（Multi-Backstack）管理的底部 Tab 切换，状态永不丢失。

---

## 🛠️ 技术栈一览

| 层级 | 技术选型 | 说明 |
| --- | --- | --- |
| **UI 框架** | Jetpack Compose | 全声明式 UI 构建，彻底告别传统 XML 布局，实现丝滑重组 |
| **导航路由** | Jetpack Navigation 3 | 谷歌新一代多返回栈导航架构，提供类型安全（Type-Safety）的参数传递，适配组件级状态管理 |
| **架构模式** | MVI + UDF | 统一状态管理，状态可预测、行为可追溯，彻底消除状态撕裂 |
| **异步处理** | Kotlin Coroutines + Flow | 响应式异步数据流管理，轻松驾驭高频及周期性状态同步 |
| **网络请求** | Retrofit 3 + OkHttp | RESTful API 健壮调用，集成高效拦截器与缓存控制 |
| **依赖注入** | Koin 4 | 超轻量级运行期 DI 框架，无编译期代码生成暗坑，完美契合 Compose |
| **图片加载** | Coil 3 | 专为 Compose 深度优化的现代化异步图片加载库 |
| **音频播放** | Media3 (ExoPlayer) | 现代 Android 媒体库，提供坚固的后台播放能力与 MediaSession 控制 |
| **视觉特效** | AGSL (Android Graphics Shading Language) | 独家高性能着色器渲染，实现硬件加速级视觉动效与毛玻璃效果 |
| **工程结构** | Gradle 多模块 | 严格按底层核心能力解耦的松耦合多模块（Multi-Module）架构，业务 Features 采用包结构在主 application 模块内聚 |

---

## 🎨 视觉特效

Meow 拒绝平庸的原生控件感，在 UI 细节上全量压榨 **AGSL (Android Graphics Shading Language)** 的渲染性能，实现了比肩系统级的视觉特效：

* **物理毛玻璃效果**：基于有向距离场（SDF, Signed Distance Fields）算出的完美圆角矩形，高保真模拟边缘折射与色散（Chromatic Aberration）物理质感。
* **动态边缘光**：引入菲涅尔效应（Fresnel Effect），使悬浮面板与顶栏根据背景主色调产生动态边缘高光，极大增强层级立体感。
* **安全归一化边界**：深度优化数学边界边界裁切，彻底消除低阶设备上常见的 1px 渲染黑线异常。

---

## 📁 项目结构

```text
Meow/
├── app/                          # 主应用模块 (Application 壳工程，包含所有的 UI 页面 and 业务 features，如 login, home, search, player, profile 等)
├── ui-glass/                     # 毛玻璃 UI 组件库 (提供 AGSL 自定义高性能毛玻璃与边缘光特效)
├── core-animation/               # 核心动画组件库 (自定义共享元素/转场动画框架)
├── core-player/                  # 播放器核心模块 (基于 Media3 ExoPlayer 的后台播放与媒体会话管理)
├── core-network/                 # 网络核心模块 (封装 Retrofit、OkHttp、Kotlinx Serialization 与网易云 API)
├── core-model/                   # 统一的数据模型定义模块 (Music, Album, Artist, UserProfile 等)
├── core-database/                # 本地数据库存储模块 (基于 Room Database 的歌单、历史及音乐缓存)
├── core-datastore/               # 本地轻量化存储模块 (基于 DataStore Preferences 与 Tink 加密实现安全凭证存储)
└── build.gradle.kts              # 项目级构建配置文件
```

---

## 🚀 快速开始

### 环境要求

* **Android Studio**：Hedgehog | 2023.1.1 或更高版本
* **JDK**：Java Development Kit 17+
* **Android SDK**：API Level 33+

### 构建与运行

请打开终端（Terminal）执行以下指令：

```bash
# 1. 克隆项目到本地
git clone git@github.com:lightningrealm/Meow.git

# 2. 进入项目根目录
cd Meow

# 3. 编译并同步项目依赖
./gradlew build

# 4. 编译、安装并运行调试版本
./gradlew installDebug
```

#### 💡 指令与参数详析（构建指引）

* **`git clone`** (*Git Clone*)：将远程 Git 仓库的代码完整复制（克隆）到你的本地目录。
* **`cd`** (*Change Directory*)：切换当前终端的工作路径至指定的文件夹。
* **`./gradlew`** (*Gradle Wrapper*)：使用项目内置的 Gradle 包装器脚本。它能确保团队内所有开发者使用完全一致 Gradle 版本，避免由于本地环境差异引发构建失败。
* **`build`**：Gradle 的标准任务，执行全量的代码编译、资源检查以及单元测试，生成构建产物。
* **`installDebug`**：Gradle 的 Android 插件专属任务。其含义为：编译项目的 **Debug（调试）** 变体代码，生成包含签名信息的 APK，并将其自动安装（Install）到当前连接的 Android 物理设备或标准模拟器上并准备运行。

---

## 📦 核心依赖一览 (`build.gradle.kts`)

```kotlin
dependencies {
    // Jetpack Compose 核心与 Material 3
    implementation("androidx.compose.ui:ui:1.11.3")
    implementation("androidx.compose.material3:material3:1.4.0")

    // Jetpack Navigation 3 新一代路由架构 (实验性)
    implementation("androidx.navigation3:navigation3-runtime:1.1.3")
    implementation("androidx.navigation3:navigation3-ui:1.1.3")
    implementation("androidx.navigation:navigation-compose:2.9.8")

    // Kotlin 协程与高性能异步流 (MVI 架构基石)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // RESTful 网络请求通信网络栈
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.4.0")

    // Koin 依赖注入框架 (Compose 专属轻量扩展)
    implementation("io.insert-koin:koin-core:4.2.2")
    implementation("io.insert-koin:koin-androidx-compose:4.2.2")

    // Coil 3 高性能异步图片加载与内存缓存管理
    implementation("io.coil-kt.coil3:coil-compose:3.5.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.5.0")

    // Media3 媒体播放架构引擎 (ExoPlayer 底层支撑与前台 Service 会话管理)
    implementation("androidx.media3:media3-exoplayer:1.10.1")
    implementation("androidx.media3:media3-session:1.10.1")

    // 本地持久化与安全存储
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("com.google.crypto.tink:tink-android:1.22.0")
}
```

---

## 📄 开源许可证 (License)

本项目采用 **MIT 许可证** 开放源代码。

> **⚠️ 特别声明**：
> 本项目属于非商业性开源学习项目，代码及相关构建产物仅供技术交流与个人学术研究使用。项目内涉及的 API 接口及数据所有权均归属于网易云音乐官方，请勿将此项目用于任何商业盈利用途。

---

## 🙏 致谢

* [NeteaseCloudMusicApi](https://github.com/Binaryify/NeteaseCloudMusicApi) —— 感谢所有为该网易云音乐 API 项目做出贡献的开发者，优秀的接口封装让现代 Android 架构落地成为可能。

---

## 👤 作者信息

* **Author**: lightningrealm
* **GitHub**: [@lightningrealm](https://github.com/lightningrealm)

如果你觉得 **Meow** 对你的 Compose 模块化学习或者高级着色器开发有所启发，欢迎为本项目点上一颗 ⭐ **Star**！这对我持续优化架构是巨大的鼓励！
