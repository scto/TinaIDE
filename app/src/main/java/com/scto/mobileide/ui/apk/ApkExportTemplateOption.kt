package com.scto.mobileide.ui.apk

import com.scto.mobileide.core.apkbuilder.ApkTemplateType
import java.io.File

data class ApkExportTemplateOption(
    val id: String,
    val label: String,
    val templateType: ApkTemplateType,
    val templateFile: File? = null
)
