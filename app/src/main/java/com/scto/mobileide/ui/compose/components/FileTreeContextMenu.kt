package com.scto.mobileide.ui.compose.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.scto.mobileide.core.commands.HostCommandExecutor
import com.scto.mobileide.core.commands.HostCommandInvocation
import com.scto.mobileide.core.commands.HostCommands
import com.scto.mobileide.core.i18n.Strings
import com.scto.mobileide.plugin.ResolvedHostMenuItem
import com.scto.mobileide.ui.compose.components.MobileDropdownMenu
import com.scto.mobileide.ui.compose.components.MobileDropdownMenuDangerItem
import com.scto.mobileide.ui.compose.components.MobileDropdownMenuDivider
import com.scto.mobileide.ui.compose.components.MobileDropdownMenuItem
import com.scto.mobileide.ui.compose.components.MobileDropdownMenuSectionHeader
import com.scto.mobileide.ui.compose.components.MobileDropdownMenuSectionTitle
import java.io.File

/**
 * 文件上下文菜单
 */
@Composable
internal fun FileContextMenu(
    file: File,
    isDirectory: Boolean,
    expanded: Boolean,
    pluginMenuItems: List<ResolvedHostMenuItem>,
    onDismiss: () -> Unit,
    onAction: (FileContextAction) -> Unit,
    modifier: Modifier = Modifier,
    hostCommandExecutor: HostCommandExecutor? = null
) {
    MobileDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        if (isDirectory) {
            MobileDropdownMenuItem(
                text = { Text(stringResource(Strings.action_new_file)) },
                onClick = { onAction(FileContextAction.NewFile(file)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                        contentDescription = null,
                        modifier = Modifier
                    )
                }
            )
            MobileDropdownMenuItem(
                text = { Text(stringResource(Strings.action_new_folder)) },
                onClick = { onAction(FileContextAction.NewFolder(file)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = null
                    )
                }
            )
            MobileDropdownMenuDivider()
        }

        MobileDropdownMenuItem(
            text = { Text(stringResource(Strings.action_rename)) },
            onClick = { onAction(FileContextAction.Rename(file)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DriveFileRenameOutline,
                    contentDescription = null
                )
            }
        )

        MobileDropdownMenuItem(
            text = { Text(stringResource(Strings.action_copy_path)) },
            onClick = { onAction(FileContextAction.CopyPath(file)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null
                )
            }
        )

        if (pluginMenuItems.isNotEmpty()) {
            MobileDropdownMenuDivider()
            MobileDropdownMenuSectionHeader {
                MobileDropdownMenuSectionTitle(
                    text = stringResource(Strings.action_more)
                )
            }
            pluginMenuItems.forEach { item ->
                MobileDropdownMenuItem(
                    text = { Text(item.title) },
                    onClick = {
                        onDismiss()
                        val handled = hostCommandExecutor?.execute(
                            item.commandId,
                            HostCommandInvocation(
                                file = file,
                                isDirectory = isDirectory
                            )
                        ) == true
                        if (!handled) {
                            toFileContextActionOrNull(item.commandId, file, isDirectory)?.let(onAction)
                        }
                    },
                    leadingIcon = null
                )
            }
        }

        MobileDropdownMenuDivider()
        MobileDropdownMenuSectionHeader {
            MobileDropdownMenuSectionTitle(
                text = stringResource(Strings.action_delete),
                color = MaterialTheme.colorScheme.error
            )
        }
        MobileDropdownMenuDangerItem(
            text = { Text(stringResource(Strings.btn_delete)) },
            onClick = { onAction(FileContextAction.Delete(file)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
            }
        )
    }
}

private fun toFileContextActionOrNull(
    commandId: String,
    file: File,
    isDirectory: Boolean
): FileContextAction? = when (commandId) {
    HostCommands.FILE_NEW -> if (isDirectory) FileContextAction.NewFile(file) else null
    HostCommands.FILE_NEW_FOLDER -> if (isDirectory) FileContextAction.NewFolder(file) else null
    HostCommands.FILE_RENAME -> FileContextAction.Rename(file)
    HostCommands.FILE_DELETE -> FileContextAction.Delete(file)
    HostCommands.FILE_COPY_PATH -> FileContextAction.CopyPath(file)
    HostCommands.FILE_COPY_NAME -> FileContextAction.CopyName(file)
    HostCommands.FILE_COPY_RELATIVE_PATH -> FileContextAction.CopyRelativePath(file)
    HostCommands.FILE_DUPLICATE -> FileContextAction.Duplicate(file)
    HostCommands.FILE_OPEN_WITH -> FileContextAction.OpenWith(file)
    HostCommands.FILE_SHARE -> FileContextAction.Share(file)
    HostCommands.FILE_REVEAL_IN_FILE_MANAGER -> FileContextAction.RevealInFileManager(file)
    else -> null
}
