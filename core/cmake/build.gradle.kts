/*
 * CMake Parser Module for MobileIDE
 * 用于解析 CMakeLists.txt 文件，支持语法高亮和代码补全
 *
 * 参考实现: https://github.com/rust-utility/cmake-parser
 * CMake 版本: v3.26
 */

plugins {
    id("mobile.android.library")
}

android {
    namespace = "com.scto.mobileide.cmake"
}

dependencies {
    implementation(project(":core:i18n"))

    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
}
