package com.scto.mobileide.ui.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.scto.mobileide.core.i18n.Strings

/**
 * Git 提交历史记录选择器对话框
 *
 * 显示最近的提交信息，方便用户快速复用
 */
@Composable
fun GitCommitHistoryDialog(
    recentMessages: List<String>,
    onMessageSelected: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    MobileAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            MobileDialogTitleText(stringResource(Strings.git_commit_history_title))
        },
        text = {
            if (recentMessages.isEmpty()) {
                MobileDialogContentColumn {
                    MobileDialogMessageCard(message = stringResource(Strings.git_commit_history_empty))
                }
            } else {
                MobileDialogContentColumn {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentMessages) { message ->
                            CommitHistoryItem(
                                message = message,
                                onClick = {
                                    onMessageSelected(message)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (recentMessages.isNotEmpty()) {
                    MobileDangerOutlinedButton(
                        text = stringResource(Strings.action_clear_history),
                        onClick = onClearHistory,
                        leadingIcon = Icons.Default.Delete
                    )
                }
                MobileTextButton(
                    text = stringResource(Strings.btn_close),
                    onClick = onDismiss
                )
            }
        }
    )
}

@Composable
private fun CommitHistoryItem(
    message: String,
    onClick: () -> Unit
) {
    MobileDialogSelectableCard(
        selected = false,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        unselectedColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
