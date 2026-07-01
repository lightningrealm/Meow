// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    id("com.google.devtools.ksp") version "2.2.10-2.0.2" apply false
}
//避免ksp版本不一致导致问题
allprojects {
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-stdlib:2.2.10")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.2.10")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.2.10")
        }
    }
}
