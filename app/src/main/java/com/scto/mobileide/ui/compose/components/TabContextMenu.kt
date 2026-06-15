package com.scto.mobileide.ui.compose.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.scto.mobileide.core.commands.HostCommandExecutor
import com.scto.mobileide.core.commands.HostCommandInvocation
import com.scto.mobileide.core.i18n.Strings
import com.scto.mobileide.plugin.PluginManager
import com.scto.mobileide.ui.compose.components.MobileDropdownMenu
import com.scto.mobileide.ui.compose.components.MobileDropdownMenuDangerItem
import com.scto.mobileide.ui.compose.components.MobileDropdownMenuItem
import java.io.File

/**
 * Tab 长按上下文菜单
 *
 * @param expanded 是否显示菜单
 * @param onDismiss 关闭菜单
 * @param onCloseCurrent 关闭当前标签页
 * @param onCloseOthers 关闭其他标签页
 * @param onCloseAll 关闭全部标签页
 */
@Composable
fun TabContextMenu(
    file: File,
    isDirty: Boolean,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onCloseCurrent: () -> Unit,
    onCloseOthers: () -> Unit,
    onCloseAll: () -> Unit,
    pluginManager: PluginManager,
    hostCommandExecutor: HostCommandExecutor?,
) {
    val enabledPlugins by pluginManager.enabledPluginsFlow.collectAsState()
    val pluginMenuItems = pluginManager.resolveEditorContextMenuItems(enabledPlugins, file, isDirty)

    MobileDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        MobileDropdownMenuItem(
            text = { Text(stringResource(Strings.action_close_current_tab)) },
            onClick = {
                onDismiss()
                onCloseCurrent()
            }
        )

        MobileDropdownMenuItem(
            text = { Text(stringResource(Strings.action_close_other_tabs)) },
            onClick = {
                onDismiss()
                onCloseOthers()
            }
        )

        MobileDropdownMenuDivider()
        MobileDropdownMenuDangerItem(
            text = { Text(stringResource(Strings.action_close_all_tabs)) },
            onClick = {
                onDismiss()
                onCloseAll()
            }
        )

        val pluginActionItems = pluginMenuItems.map { item -> item.title to item.commandId }

        if (pluginActionItems.isNotEmpty()) {
            MobileDropdownMenuDivider()
            MobileDropdownMenuSectionHeader {
                MobileDropdownMenuSectionTitle(
                    text = stringResource(Strings.action_more)
                )
            }
            pluginActionItems.forEach { (title, action) ->
                MobileDropdownMenuItem(
                    text = { Text(title) },
                    onClick = {
                        onDismiss()
                        hostCommandExecutor?.execute(
                            action,
                            HostCommandInvocation(
                                file = file,
                                isDirectory = file.isDirectory,
                                isDirty = isDirty
                            )
                        )
                    }
                )
            }
        }
    }
}
