package com.scto.mobileide.file

import java.io.File

/**
 * 最近文件读取接口。
 */
interface IRecentFilesProvider {
    fun getRecentFiles(): List<File>
}
