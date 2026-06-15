package com.scto.mobileide.ui.compose.components.editor

import com.scto.mobileide.editor.io.FileCharsetDetector
import java.io.File
import java.nio.charset.Charset

/**
 * 文件编码检测工具
 */
object FileEncodingDetector {

    fun detectCharset(file: File): Charset = FileCharsetDetector.detect(file)
}
