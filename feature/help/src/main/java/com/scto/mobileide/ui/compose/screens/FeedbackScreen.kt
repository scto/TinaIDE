package com.scto.mobileide.ui.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import com.scto.mobileide.core.i18n.Strings
import com.scto.mobileide.data.model.FeedbackCategory
import com.scto.mobileide.ui.compose.viewmodel.FeedbackViewModel
import com.scto.mobileide.ui.compose.components.MobileTopBar
import com.scto.mobileide.ui.compose.components.MobileSpacing
import com.scto.mobileide.ui.compose.components.MobileTextField
import com.scto.mobileide.ui.compose.components.MobilePrimaryButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedbackScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: FeedbackViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    // 提交成功后返回
    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            kotlinx.coroutines.delay(1500)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            MobileTopBar(
                title = stringResource(Strings.feedback_title),
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = {
            if (uiState.submitError != null) {
                Snackbar(
                    modifier = Modifier.padding(MobileSpacing.xl),
                    action = {
                        TextButton(onClick = viewModel::dismissError) {
                            Text(stringResource(Strings.dismiss))
                        }
                    }
                ) {
                    Text(uiState.submitError ?: "")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(MobileSpacing.xl)
        ) {
            // 分类选择
            Text(
                text = stringResource(Strings.feedback_category_label),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = MobileSpacing.md)
            )
            
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(MobileSpacing.md),
                verticalArrangement = Arrangement.spacedBy(MobileSpacing.md),
                modifier = Modifier.padding(bottom = MobileSpacing.xl)
            ) {
                FeedbackCategory.values().forEach { category ->
                    FilterChip(
                        selected = uiState.category == category,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(stringResource(category.labelRes)) },
                        leadingIcon = if (uiState.category == category) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null
                    )
                }
            }
            
            // 标题输入
            MobileTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = stringResource(Strings.feedback_title_label),
                placeholder = stringResource(Strings.feedback_title_hint),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val trimmedLength = uiState.title.trim().length
                        val remaining = 5 - trimmedLength
                        if (uiState.titleError != null) {
                            Text(
                                text = uiState.titleError ?: "",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (remaining > 0) {
                            Text(
                                text = stringResource(Strings.feedback_min_chars_hint, remaining),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Text("${uiState.title.length}/100")
                    }
                },
                isError = uiState.titleError != null
            )

            Spacer(modifier = Modifier.height(MobileSpacing.xl))

            // 内容输入
            MobileTextField(
                value = uiState.content,
                onValueChange = viewModel::updateContent,
                label = stringResource(Strings.feedback_content_label),
                placeholder = stringResource(Strings.feedback_content_hint),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val trimmedLength = uiState.content.trim().length
                        val remaining = 10 - trimmedLength
                        if (uiState.contentError != null) {
                            Text(
                                text = uiState.contentError ?: "",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (remaining > 0) {
                            Text(
                                text = stringResource(Strings.feedback_min_chars_hint, remaining),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Text("${uiState.content.length}/5000")
                    }
                },
                isError = uiState.contentError != null
            )
            
            Spacer(modifier = Modifier.height(MobileSpacing.xxxl))

            // 提交按钮
            MobilePrimaryButton(
                text = if (uiState.submitSuccess) {
                    stringResource(Strings.feedback_submit_success)
                } else {
                    stringResource(Strings.feedback_submit)
                },
                onClick = viewModel::submitFeedback,
                enabled = uiState.canSubmit,
                modifier = Modifier.fillMaxWidth()
            )
            
            // 提示信息
            if (!uiState.isSubmitting && !uiState.submitSuccess) {
                Text(
                    text = stringResource(Strings.feedback_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
