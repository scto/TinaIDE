package com.scto.mobileide.ui.compose.screens.main.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scto.mobileide.core.i18n.Strings
import com.scto.mobileide.ui.compose.components.MarkdownViewer
import com.scto.mobileide.ui.compose.components.MobileCustomDialog
import com.scto.mobileide.ui.compose.components.MobileCustomDialogHeader
import com.scto.mobileide.ui.compose.components.MobileDialogActionRow
import com.scto.mobileide.ui.compose.components.MobilePrimaryButton
import com.scto.mobileide.ui.compose.components.MobileTextButton
import com.scto.mobileide.update.AppUpdateInfo

@Composable
fun AppUpdateDialog(
    updateInfo: AppUpdateInfo,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
) {
    MobileCustomDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 12.dp,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MobileCustomDialogHeader(
                title = stringResource(Strings.app_update_dialog_title),
                subtitle = stringResource(Strings.app_update_dialog_subtitle, updateInfo.releaseName),
            )

            Text(
                text = stringResource(
                    Strings.app_update_dialog_message,
                    updateInfo.currentVersionName,
                    updateInfo.tagName,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            updateInfo.assetName?.takeIf(String::isNotBlank)?.let { assetName ->
                Text(
                    text = stringResource(Strings.app_update_asset_name, assetName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            updateInfo.releaseNotes?.takeIf(String::isNotBlank)?.let { notes ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Strings.app_update_release_notes),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        MarkdownViewer(
                            markdown = notes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp),
                        )
                    }
                }
            }

            MobileDialogActionRow {
                MobileTextButton(
                    text = stringResource(Strings.app_update_action_later),
                    onClick = onDismiss,
                )
                MobilePrimaryButton(
                    text = stringResource(Strings.app_update_action_download),
                    onClick = onDownload,
                )
            }
        }
    }
}
