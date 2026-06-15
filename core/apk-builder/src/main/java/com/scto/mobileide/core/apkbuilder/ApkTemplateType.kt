package com.scto.mobileide.core.apkbuilder

enum class ApkTemplateType(val templateFileName: String) {
    NATIVE_ACTIVITY("template-native-activity.apk"),
    SDL3("template-sdl3.apk"),
    TERMINAL("template-terminal.apk")
}
