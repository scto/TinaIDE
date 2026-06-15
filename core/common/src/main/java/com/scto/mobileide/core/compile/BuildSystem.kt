package com.scto.mobileide.core.compile

/**
 * 构建系统类型
 */
enum class BuildSystem {
    /** 单文件编译（无构建系统） */
    SINGLE_FILE,

    /** CMake 项目 */
    CMAKE,

    /** Makefile 项目 */
    MAKE,

    /** MobileIDE 插件项目 */
    PLUGIN,

    /** Gradle 项目 */
    GRADLE,

    /** 未知类型 */
    UNKNOWN
}
