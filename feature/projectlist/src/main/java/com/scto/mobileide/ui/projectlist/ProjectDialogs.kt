package com.scto.mobileide.ui.projectlist

import com.scto.mobileide.core.common.simplifyPath
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scto.mobileide.project.ProjectBuildSystem
import com.scto.mobileide.project.ProjectListItem
import com.scto.mobileide.ui.compose.components.MobileShapes
import com.scto.mobileide.ui.compose.components.MobilePrimaryButton
import com.scto.mobileide.ui.compose.components.MobileDangerButton
import com.scto.mobileide.ui.compose.components.MobileOutlinedButton
import com.scto.mobileide.ui.compose.components.MobileAlertDialog
import com.scto.mobileide.ui.compose.components.MobileDialogCard
import com.scto.mobileide.ui.compose.components.MobileDialogContentColumn
import com.scto.mobileide.ui.compose.components.MobileDialogMessageCard
import com.scto.mobileide.ui.compose.components.MobileSemanticColors
import com.scto.mobileide.ui.compose.components.MobileDialogTitleText
import com.scto.mobileide.ui.compose.components.MobileTextButton
import com.scto.mobileide.ui.compose.icons.rememberMobilePainter
import java.io.File
import com.scto.mobileide.core.i18n.Strings

/**
 * Git 克隆对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitCloneDialog(
    defaultPath: String,
    onDismiss: () -> Unit,
    onClone: (url: String, projectName: String, branch: String) -> Unit
) {
    var gitUrl by remember { mutableStateOf("") }
    var projectName by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }
    var urlError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val errorProjectNameInvalid = stringResource(Strings.error_project_name_invalid)
    val errorGitUrlEmpty = stringResource(Strings.error_git_url_empty)
    val errorGitUrlInvalid = stringResource(Strings.error_git_url_invalid)
    val errorProjectNameEmptyDialog = stringResource(Strings.error_project_name_empty_dialog)

    // 从 Git URL 自动提取项目名称
    LaunchedEffect(gitUrl) {
        if (gitUrl.isNotBlank() && projectName.isBlank()) {
            val extractedName = extractProjectNameFromUrl(gitUrl)
            if (extractedName.isNotBlank()) {
                projectName = extractedName
            }
        }
    }

    MobileAlertDialog(
        onDismissRequest = onDismiss,
        title = { MobileDialogTitleText(stringResource(Strings.dialog_title_git_import)) },
        text = {
            MobileDialogContentColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Git URL 输入
                OutlinedTextField(
                    value = gitUrl,
                    onValueChange = {
                        gitUrl = it
                        urlError = null
                        // 自动提取项目名称
                        if (projectName.isBlank() || projectName == extractProjectNameFromUrl(gitUrl.dropLast(1))) {
                            projectName = extractProjectNameFromUrl(it)
                        }
                    },
                    label = { Text(stringResource(Strings.label_git_url)) },
                    placeholder = { Text(stringResource(Strings.placeholder_git_url)) },
                    isError = urlError != null,
                    supportingText = urlError?.let { { Text(it) } } ?: {
                        Text(stringResource(Strings.hint_git_platforms))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 项目名称输入
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { c -> c.isLetterOrDigit() || c == '_' || c == '-' }.filter { it.code <= 127 }
                        projectName = filtered
                        nameError = if (filtered != newValue) errorProjectNameInvalid else null
                    },
                    label = { Text(stringResource(Strings.label_project_name)) },
                    placeholder = { Text(stringResource(Strings.placeholder_project_name)) },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 分支输入
                OutlinedTextField(
                    value = branch,
                    onValueChange = { branch = it },
                    label = { Text(stringResource(Strings.label_branch_optional)) },
                    placeholder = { Text(stringResource(Strings.placeholder_branch)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 项目位置显示
                OutlinedTextField(
                    value = formatPathForDisplay(defaultPath, projectName, context),
                    onValueChange = {},
                    label = { Text(stringResource(Strings.label_project_location)) },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 提示信息
                MobileDialogCard(
                    color = MobileSemanticColors.Project.tipCardBg,
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = rememberMobilePainter(com.scto.mobileide.core.designsystem.R.drawable.ic_lightbulb_hint),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MobileSemanticColors.Project.tipCardIcon
                        )
                        Text(
                            stringResource(Strings.hint_clone_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = MobileSemanticColors.Project.tipCardText
                        )
                    }
                }
            }
        },
        confirmButton = {
            MobilePrimaryButton(
                text = stringResource(Strings.btn_clone),
                onClick = {
                    // 验证输入
                    var hasError = false
                    if (gitUrl.isBlank()) {
                        urlError = errorGitUrlEmpty
                        hasError = true
                    } else if (!isValidGitUrl(gitUrl)) {
                        urlError = errorGitUrlInvalid
                        hasError = true
                    }
                    if (projectName.isBlank()) {
                        nameError = errorProjectNameEmptyDialog
                        hasError = true
                    }
                    if (!hasError) {
                        onClone(gitUrl.trim(), projectName.trim(), branch.trim())
                    }
                }
            )
        },
        dismissButton = {
            MobileTextButton(
                text = stringResource(Strings.btn_cancel),
                onClick = onDismiss
            )
        }
    )
}
/**
 * 重命名项目对话框
 */
@Composable
fun RenameProjectDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }
    var nameError by remember { mutableStateOf<String?>(null) }
    val errorProjectNameInvalid = stringResource(Strings.error_project_name_invalid)
    val errorNameEmpty = stringResource(Strings.error_name_empty)
    MobileAlertDialog(
        onDismissRequest = onDismiss,
        title = { MobileDialogTitleText(stringResource(Strings.dialog_title_rename)) },
        text = {
            MobileDialogContentColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { value ->
                        val filtered = value.filter { c -> c.isLetterOrDigit() || c == '_' || c == '-' }.filter { it.code <= 127 }
                        newName = filtered
                        nameError = if (filtered != value) errorProjectNameInvalid else null
                    },
                    label = { Text(stringResource(Strings.label_new_name)) },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            MobilePrimaryButton(
                text = stringResource(Strings.btn_confirm),
                onClick = {
                    if (newName.isBlank()) {
                        nameError = errorNameEmpty
                    } else if (newName == currentName) {
                        onDismiss()
                    } else {
                        onConfirm(newName)
                    }
                }
            )
        },
        dismissButton = {
            MobileTextButton(
                text = stringResource(Strings.btn_cancel),
                onClick = onDismiss
            )
        }
    )
}
/**
 * 项目信息对话框
 */
@Composable
fun ProjectInfoDialog(
    project: ProjectListItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isGitProject = remember(project.dir) {
        File(project.dir, ".git").exists()
    }

    // 根据构建系统、语言和 Git 状态计算标签列表
    val projectTags = remember(project.buildSystem, project.primaryLanguage, isGitProject) {
        buildList {
            if (isGitProject) {
                add(ProjectTag.GIT)
            }
            when (project.buildSystem) {
                ProjectBuildSystem.CMAKE -> add(ProjectTag.CMAKE)
                ProjectBuildSystem.MAKE -> add(ProjectTag.MAKEFILE)
                ProjectBuildSystem.PLUGIN -> add(ProjectTag.PLUGIN)
                else -> { /* 单文件或未知构建系统不显示构建系统标签 */ }
            }
            // 根据主要语言添加语言标签
            val languageTag = project.primaryLanguage?.let { ProjectTag.fromLanguage(it) }
            if (languageTag != null) {
                add(languageTag)
            } else if (project.buildSystem != ProjectBuildSystem.PLUGIN) {
                // 默认显示 C/C++ 标签（兼容旧项目）
                add(ProjectTag.C_CPP)
            }
        }
    }

    // 计算项目大小
    val projectSize = remember(project.dir) {
        calculateDirectorySize(project.dir)
    }

    // 获取文件数量
    val fileCount = remember(project.dir) {
        countFiles(project.dir)
    }

    // 获取最后修改时间
    val lastModified = remember(project.dir) {
        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(project.dir.lastModified()))
    }

    MobileAlertDialog(
        onDismissRequest = onDismiss,
        title = { MobileDialogTitleText(stringResource(Strings.dialog_title_project_info)) },
        text = {
            MobileDialogContentColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow(label = stringResource(Strings.label_project_name), value = project.displayName)
                // 项目标签行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Strings.label_project_tags),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        projectTags.forEach { tag ->
                            ProjectTagChip(tag = tag)
                        }
                    }
                }
                InfoRow(label = stringResource(Strings.info_project_path), value = simplifyPath(project.dir.absolutePath, context))
                InfoRow(label = stringResource(Strings.info_project_size), value = formatFileSize(projectSize))
                InfoRow(label = stringResource(Strings.info_file_count), value = stringResource(Strings.info_file_count_value, fileCount))
                InfoRow(label = stringResource(Strings.info_last_modified), value = lastModified)
            }
        },
        confirmButton = {
            MobileTextButton(
                text = stringResource(Strings.btn_close),
                onClick = onDismiss
            )
        }
    )
}
/**
 * 删除确认对话框
 */
@Composable
fun DeleteConfirmDialog(
    projectName: String,
    confirmMessage: String,
    warningMessage: String,
    confirmButtonText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    MobileAlertDialog(
        onDismissRequest = onDismiss,
        title = { MobileDialogTitleText(stringResource(Strings.dialog_title_delete_project)) },
        text = {
            MobileDialogContentColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MobileDialogMessageCard(message = confirmMessage)
                MobileDialogMessageCard(
                    message = warningMessage,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.36f),
                    textColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        confirmButton = {
            MobileDangerButton(
                text = confirmButtonText,
                onClick = onConfirm
            )
        },
        dismissButton = {
            MobileTextButton(
                text = stringResource(Strings.btn_cancel),
                onClick = onDismiss
            )
        }
    )
}
