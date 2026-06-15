package com.scto.mobileide.ui.compose.screens.packages
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scto.mobileide.core.i18n.Strings
import com.scto.mobileide.core.packages.InstalledPackageMetadata
import org.koin.androidx.compose.koinViewModel
import com.scto.mobileide.core.packages.model.*
import com.scto.mobileide.ui.compose.components.MobileTopBar
import com.scto.mobileide.ui.compose.components.MobileSpacing
import com.scto.mobileide.ui.compose.components.MobileShapes
import com.scto.mobileide.ui.compose.components.PluginCardSkeleton
import com.scto.mobileide.ui.compose.components.MobilePrimaryButton
import com.scto.mobileide.ui.compose.components.MobileTextButton
import com.scto.mobileide.ui.compose.components.MobileAlertDialog
import com.scto.mobileide.ui.compose.components.MobileDialogCard
import com.scto.mobileide.ui.compose.components.MobileDialogContentColumn
import com.scto.mobileide.ui.compose.components.MobileDialogMessageCard
import com.scto.mobileide.ui.compose.components.MobileDialogTitleText
import com.scto.mobileide.ui.compose.components.MobileInfoDialog
import com.scto.mobileide.ui.compose.components.MobileSearchField
import com.scto.mobileide.ui.compose.components.MobileOutlinedButton
import com.scto.mobileide.ui.compose.components.MobileDangerButton
import com.scto.mobileide.ui.compose.components.MobileBackHandlers
import com.scto.mobileide.ui.compose.components.mobileBackAction

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PackageManagerScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: PackageManagerViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    // 处理系统返回键：优先处理内部子界面的返回
    MobileBackHandlers(
        mobileBackAction(enabled = uiState.currentDetailPackage != null) {
            viewModel.closePackageDetails()
        }
    )

    if (uiState.currentDetailPackage != null) {
        val pkg = uiState.currentDetailPackage!!
        PackageDetailScreen(
            pkg = pkg,
            installedMetadata = uiState.installedMetadata[pkg.id],
            installState = uiState.installStates[pkg.id] ?: PackageInstallState(),
            installEvent = uiState.currentInstallEvent,
            onInstall = { platform -> viewModel.installPackage(pkg.id, platform) },
            onUninstall = { platform -> viewModel.requestUninstall(pkg.id, platform) },
            onNavigateBack = viewModel::closePackageDetails,
            onDependencyClick = viewModel::navigateToDependency
        )
    } else {

    Scaffold(
        topBar = {
            if (uiState.isSelectionMode) {
                MobileTopBar(
                    title = stringResource(Strings.pkg_manager_selected, uiState.selectedPackageIds.size),
                    navigationIcon = {
                        IconButton(onClick = viewModel::toggleSelectionMode) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(Strings.btn_cancel))
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::selectAll) {
                            Icon(Icons.Default.SelectAll, contentDescription = stringResource(Strings.pkg_manager_select))
                        }
                        IconButton(onClick = viewModel::clearSelection) {
                            Icon(Icons.Default.Deselect, contentDescription = stringResource(Strings.btn_deselect_all))
                        }
                    }
                )
            } else {
                MobileTopBar(
                    title = stringResource(Strings.pkg_manager_title),
                    onNavigateBack = onNavigateBack,
                    actions = {
                        if (uiState.availableUpdates.isNotEmpty()) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) {
                                Text("${uiState.availableUpdates.size}")
                            }
                            TextButton(onClick = viewModel::updateAllPackages) {
                                Text(stringResource(Strings.pkg_manager_update_all))
                            }
                        }
                        IconButton(onClick = viewModel::checkForUpdates) {
                            Icon(Icons.Default.Update, contentDescription = stringResource(Strings.pkg_manager_check_updates))
                        }
                        IconButton(onClick = viewModel::refreshPackages) {
                            Icon(Icons.Default.Refresh, contentDescription = stringResource(Strings.pkg_manager_refresh))
                        }
                        IconButton(onClick = viewModel::toggleSelectionMode) {
                            Icon(Icons.Default.Checklist, contentDescription = stringResource(Strings.pkg_manager_select))
                        }
                    }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(visible = uiState.isSelectionMode && uiState.selectedPackageIds.isNotEmpty()) {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MobileSpacing.xl),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MobileDangerButton(
                            text = stringResource(Strings.pkg_manager_uninstall_linux),
                            onClick = { viewModel.batchUninstall(Platform.LINUX) },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(MobileSpacing.md))
                        MobileDangerButton(
                            text = stringResource(Strings.pkg_manager_uninstall_android),
                            onClick = { viewModel.batchUninstall(Platform.ANDROID) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                query = filterState.searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MobileSpacing.xl, vertical = MobileSpacing.md)
            )

            if (uiState.isLoading) {
                // 显示骨架屏
                LazyColumn(
                    contentPadding = PaddingValues(MobileSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(MobileSpacing.lg),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(5) {
                        PluginCardSkeleton()
                    }
                }
            } else if (uiState.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: stringResource(Strings.pkg_manager_error_unknown),
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(MobileSpacing.xl))
                        MobilePrimaryButton(
                            text = stringResource(Strings.pkg_manager_retry),
                            onClick = viewModel::loadPackages
                        )
                    }
                }
            } else if (uiState.filteredPackages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Strings.pkg_manager_no_available_packages),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(MobileSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(MobileSpacing.lg)
                ) {
                    items(uiState.filteredPackages, key = { it.id }) { pkg ->
                        PackageCard(
                            pkg = pkg,
                            installedMetadata = uiState.installedMetadata[pkg.id],
                            installState = uiState.installStates[pkg.id] ?: PackageInstallState(),
                            isSelectionMode = uiState.isSelectionMode,
                            isSelected = pkg.id in uiState.selectedPackageIds,
                            onInstallLinux = { viewModel.installPackage(pkg.id, Platform.LINUX) },
                            onInstallAndroid = { viewModel.installPackage(pkg.id, Platform.ANDROID) },
                            onUninstallLinux = { viewModel.requestUninstall(pkg.id, Platform.LINUX) },
                            onUninstallAndroid = { viewModel.requestUninstall(pkg.id, Platform.ANDROID) },
                            onClick = {
                                if (uiState.isSelectionMode) {
                                    viewModel.togglePackageSelection(pkg.id)
                                } else {
                                    viewModel.showPackageDetails(pkg.id)
                                }
                            },
                            onLongClick = {
                                if (!uiState.isSelectionMode) {
                                    viewModel.toggleSelectionMode()
                                    viewModel.togglePackageSelection(pkg.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    } // end else

    dialogState?.let { state ->
        when (state) {
            is PackageDialogState.Installing -> {
                InstallProgressDialog(
                    packageName = state.packageName,
                    platform = state.platform,
                    event = state.event,
                    onDismiss = {}
                )
            }
            is PackageDialogState.InstallComplete -> {
                InstallCompleteDialog(
                    result = state.result,
                    onDismiss = viewModel::dismissDialog
                )
            }
            is PackageDialogState.UninstallConfirm -> {
                UninstallConfirmDialog(
                    packageInfo = state.packageInfo,
                    platform = state.platform,
                    dependentPackages = state.dependentPackages,
                    onConfirm = { viewModel.confirmUninstall(state.packageId, state.platform) },
                    onDismiss = viewModel::dismissDialog
                )
            }
            is PackageDialogState.UninstallComplete -> {
                UninstallCompleteDialog(
                    result = state.result,
                    onDismiss = viewModel::dismissDialog
                )
            }
            is PackageDialogState.PackageDetails -> {
                PackageDetailsDialog(
                    pkg = state.packageInfo,
                    installedMetadata = uiState.installedMetadata[state.packageInfo.id],
                    onDismiss = viewModel::dismissDialog
                )
            }
            is PackageDialogState.BatchInstalling -> {
                BatchInstallProgressDialog(
                    currentIndex = state.currentIndex,
                    totalCount = state.totalCount,
                    currentPackageName = state.currentPackageName,
                    platform = state.platform,
                    event = state.event
                )
            }
            is PackageDialogState.BatchInstallComplete -> {
                MobileInfoDialog(
                    title = stringResource(Strings.pkg_manager_batch_complete_title),
                    message = stringResource(Strings.pkg_manager_batch_complete_msg, state.totalCount, state.platform.name),
                    confirmText = stringResource(Strings.btn_confirm),
                    onDismiss = viewModel::dismissDialog
                )
            }
            is PackageDialogState.BatchPlatformSelect -> {}
            is PackageDialogState.BatchUpdating -> {
                BatchUpdateProgressDialog(
                    currentIndex = state.currentIndex,
                    totalCount = state.totalCount,
                    currentPackageName = state.currentPackageName,
                    event = state.event
                )
            }
            is PackageDialogState.BatchUpdateComplete -> {
                MobileInfoDialog(
                    title = stringResource(Strings.pkg_manager_update_complete_title),
                    message = stringResource(Strings.pkg_manager_update_complete_msg, state.totalCount),
                    confirmText = stringResource(Strings.btn_confirm),
                    onDismiss = viewModel::dismissDialog
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    MobileSearchField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = stringResource(Strings.pkg_manager_search_hint),
        modifier = modifier
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PackageCard(
    pkg: GUIPackage,
    installedMetadata: InstalledPackageMetadata? = null,
    installState: PackageInstallState,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onInstallLinux: () -> Unit,
    onInstallAndroid: () -> Unit,
    onUninstallLinux: () -> Unit,
    onUninstallAndroid: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val packageVersion = installedMetadata?.version ?: pkg.android?.version ?: pkg.linux?.version
    val upstreamVersion = installedMetadata?.upstreamVersion

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier.padding(MobileSpacing.xl),
            verticalAlignment = Alignment.Top
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = MobileSpacing.lg)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MobileSpacing.md)
                    ) {
                        Text(
                            text = pkg.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (pkg.isBundled) {
                            Text(
                                text = stringResource(Strings.pkg_manager_bundled),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.tertiaryContainer,
                                        RoundedCornerShape(MobileShapes.ExtraSmallCorner)
                                    )
                                    .padding(horizontal = MobileSpacing.sm, vertical = MobileSpacing.xxs)
                            )
                        }
                    }
                    pkg.linux?.version?.let { version ->
                        Text(
                            text = "v${packageVersion ?: version}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (pkg.linux?.version == null) {
                        packageVersion?.let { version ->
                            Text(
                                text = "v$version",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                pkg.description?.let { desc ->
                    Spacer(modifier = Modifier.height(MobileSpacing.xs))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                installedMetadata?.let { metadata ->
                    Spacer(modifier = Modifier.height(MobileSpacing.xs))
                    val versionSummary = buildList {
                        add(stringResource(Strings.pkg_manager_card_package_version, metadata.version))
                        metadata.upstreamVersion?.let { add(stringResource(Strings.pkg_manager_card_library_version, it)) }
                    }.joinToString("  ·  ")
                    Text(
                        text = versionSummary,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!isSelectionMode) {
                    Spacer(modifier = Modifier.height(MobileSpacing.lg))

                    if (pkg.linux != null) {
                        PlatformRow(
                            platformLabel = "Linux",
                            platformIcon = "\uD83D\uDC27",
                            state = installState.linux,
                            onInstall = onInstallLinux,
                            onUninstall = onUninstallLinux
                        )
                    }

                    if (pkg.android != null) {
                        Spacer(modifier = Modifier.height(MobileSpacing.md))
                        PlatformRow(
                            platformLabel = "Android",
                            platformIcon = "\uD83E\uDD16",
                            state = installState.android,
                            onInstall = onInstallAndroid,
                            onUninstall = onUninstallAndroid
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlatformRow(
    platformLabel: String,
    platformIcon: String,
    state: PlatformInstallState,
    onInstall: () -> Unit,
    onUninstall: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = platformIcon)
            Spacer(modifier = Modifier.width(MobileSpacing.md))
            Text(text = platformLabel, style = MaterialTheme.typography.bodyMedium)
        }

        when (state) {
            is PlatformInstallState.NotInstalled -> {
                MobilePrimaryButton(
                    text = stringResource(Strings.pkg_manager_btn_install),
                    onClick = onInstall,
                    contentPadding = PaddingValues(horizontal = MobileSpacing.xl, vertical = MobileSpacing.xs)
                )
            }
            is PlatformInstallState.Installed -> {
                MobileOutlinedButton(
                    text = stringResource(Strings.pkg_manager_btn_installed),
                    onClick = onUninstall,
                    leadingIcon = Icons.Default.Check,
                    contentPadding = PaddingValues(horizontal = MobileSpacing.xl, vertical = MobileSpacing.xs)
                )
            }
            is PlatformInstallState.Installing -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(MobileSpacing.md))
                    Text("${(state.progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                }
            }
            is PlatformInstallState.UpdateAvailable -> {
                MobilePrimaryButton(
                    text = stringResource(Strings.pkg_manager_btn_update),
                    onClick = onInstall,
                    contentPadding = PaddingValues(horizontal = MobileSpacing.xl, vertical = MobileSpacing.xs)
                )
            }
        }
    }
}

@Composable
private fun InstallProgressDialog(
    packageName: String,
    platform: Platform,
    event: InstallProgressEvent,
    onDismiss: () -> Unit
) {
    MobileAlertDialog(
        onDismissRequest = {},
        title = { MobileDialogTitleText(stringResource(Strings.pkg_manager_installing_title, packageName)) },
        text = {
            MobileDialogContentColumn {
                MobileDialogCard(verticalArrangement = Arrangement.spacedBy(MobileSpacing.md)) {
                    when (event) {
                        is InstallProgressEvent.Preparing -> {
                            Text(stringResource(Strings.pkg_manager_progress_preparing, event.message))
                        }
                        is InstallProgressEvent.Downloading -> {
                            LinearProgressIndicator(
                                progress = { event.progress },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                stringResource(
                                    Strings.pkg_manager_progress_downloading,
                                    (event.progress * 100).toInt()
                                )
                            )
                        }
                        is InstallProgressEvent.Verifying -> {
                            Text(stringResource(Strings.pkg_manager_progress_verifying, event.message))
                        }
                        is InstallProgressEvent.Extracting -> {
                            LinearProgressIndicator(
                                progress = { event.progress },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(stringResource(Strings.pkg_manager_progress_extracting))
                        }
                        is InstallProgressEvent.Installing -> {
                            Text(stringResource(Strings.pkg_manager_progress_installing, event.message))
                        }
                        is InstallProgressEvent.Completed -> {
                            Text(stringResource(Strings.pkg_manager_progress_completed))
                        }
                        is InstallProgressEvent.Failed -> {
                            Text(
                                stringResource(
                                    Strings.pkg_manager_progress_failed,
                                    event.error.toDisplayMessage()
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (event is InstallProgressEvent.Completed || event is InstallProgressEvent.Failed) {
                MobileTextButton(
                    text = stringResource(Strings.btn_close),
                    onClick = onDismiss
                )
            }
        }
    )
}

@Composable
private fun InstallCompleteDialog(
    result: InstallResult,
    onDismiss: () -> Unit
) {
    when (result) {
        is InstallResult.Success -> {
            // 安装成功：提示用户重新打开文件
            MobileAlertDialog(
                onDismissRequest = onDismiss,
                title = { MobileDialogTitleText(stringResource(Strings.pkg_manager_install_complete)) },
                text = {
                    MobileDialogContentColumn {
                        MobileDialogMessageCard(
                            message = stringResource(
                                Strings.pkg_manager_install_success_msg,
                                result.packageId,
                                result.version
                            )
                        )
                        MobileDialogMessageCard(
                            message = stringResource(Strings.pkg_manager_reopen_file_hint),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            textColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                confirmButton = {
                    MobilePrimaryButton(
                        text = stringResource(Strings.btn_confirm),
                        onClick = onDismiss
                    )
                }
            )
        }
        is InstallResult.Failure -> {
            // 安装失败：只显示错误信息
            MobileInfoDialog(
                title = stringResource(Strings.pkg_manager_install_failed),
                message = result.error.toDisplayMessage(),
                confirmText = stringResource(Strings.btn_confirm),
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun UninstallConfirmDialog(
    packageInfo: GUIPackage,
    platform: Platform,
    dependentPackages: List<String>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    MobileAlertDialog(
        onDismissRequest = onDismiss,
        title = { MobileDialogTitleText(stringResource(Strings.pkg_manager_uninstall_title, packageInfo.name)) },
        text = {
            MobileDialogContentColumn {
                MobileDialogMessageCard(
                    message = stringResource(
                        Strings.pkg_manager_uninstall_message,
                        platform.name.lowercase(),
                        packageInfo.name
                    )
                )
                if (dependentPackages.isNotEmpty()) {
                    MobileDialogCard(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(Strings.pkg_manager_uninstall_warning),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        dependentPackages.forEach { dependentPackage ->
                            Text(
                                text = "\u2022 $dependentPackage",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            MobileDangerButton(
                text = stringResource(Strings.pkg_manager_btn_uninstall),
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

@Composable
private fun UninstallCompleteDialog(
    result: UninstallResult,
    onDismiss: () -> Unit
) {
    when (result) {
        is UninstallResult.Success -> {
            // 卸载成功：提示用户重新打开文件
            MobileAlertDialog(
                onDismissRequest = onDismiss,
                title = { MobileDialogTitleText(stringResource(Strings.pkg_manager_uninstall_complete)) },
                text = {
                    MobileDialogContentColumn {
                        MobileDialogMessageCard(
                            message = stringResource(
                                Strings.pkg_manager_uninstall_success_msg,
                                result.packageId
                            )
                        )
                        MobileDialogMessageCard(
                            message = stringResource(Strings.pkg_manager_reopen_file_hint),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            textColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                confirmButton = {
                    MobilePrimaryButton(
                        text = stringResource(Strings.btn_confirm),
                        onClick = onDismiss
                    )
                }
            )
        }
        is UninstallResult.Failure -> {
            // 卸载失败：只显示错误信息
            MobileInfoDialog(
                title = stringResource(Strings.pkg_manager_uninstall_failed),
                message = result.error.toDisplayMessage(),
                confirmText = stringResource(Strings.btn_confirm),
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun PackageDetailsDialog(
    pkg: GUIPackage,
    installedMetadata: InstalledPackageMetadata?,
    onDismiss: () -> Unit
) {
    MobileAlertDialog(
        onDismissRequest = onDismiss,
        title = { MobileDialogTitleText(pkg.name) },
        text = {
            MobileDialogContentColumn {
                pkg.description?.let { description ->
                    MobileDialogMessageCard(message = description)
                }

                if (pkg.homepage != null || pkg.category != null) {
                    MobileDialogCard(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        pkg.homepage?.let {
                            Text(
                                text = stringResource(Strings.pkg_manager_detail_homepage, it),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        pkg.category?.let {
                            Text(
                                text = stringResource(Strings.pkg_manager_detail_category, it),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (pkg.linux != null || pkg.android != null) {
                    MobileDialogCard(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        pkg.linux?.let { linuxPkg ->
                            val linuxArtifactTypeLabel = stringResource(linuxPkg.artifactType.labelResId())
                            Text(
                                text = stringResource(
                                    Strings.pkg_manager_detail_linux,
                                    linuxPkg.version,
                                    linuxPkg.installType.name.lowercase()
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = stringResource(
                                    Strings.pkg_manager_detail_artifact_type,
                                    linuxArtifactTypeLabel
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        pkg.android?.let { androidPkg ->
                            val androidArtifactTypeLabel = stringResource(androidPkg.artifactType.labelResId())
                            Text(
                                text = stringResource(
                                    Strings.pkg_manager_detail_android,
                                    androidPkg.version,
                                    androidPkg.installType.name.lowercase()
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = stringResource(
                                    Strings.pkg_manager_detail_artifact_type,
                                    androidArtifactTypeLabel
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                            androidPkg.abi?.let { abi ->
                                Text(
                                    text = stringResource(Strings.pkg_manager_detail_abi, abi.joinToString()),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                installedMetadata?.let { metadata ->
                    MobileDialogCard(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(Strings.pkg_detail_package_version, metadata.version),
                            style = MaterialTheme.typography.bodySmall
                        )
                        metadata.packageRevision?.let {
                            Text(
                                text = stringResource(Strings.pkg_detail_package_revision, it),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        metadata.upstreamName?.let {
                            Text(
                                text = stringResource(Strings.pkg_detail_upstream_name, it),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        metadata.upstreamVersion?.let {
                            Text(
                                text = stringResource(Strings.pkg_detail_library_version, it),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        metadata.upstreamTag?.let {
                            Text(
                                text = stringResource(Strings.pkg_detail_upstream_tag, it),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
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

@Composable
private fun BatchInstallProgressDialog(
    currentIndex: Int,
    totalCount: Int,
    currentPackageName: String,
    platform: Platform,
    event: InstallProgressEvent
) {
    MobileAlertDialog(
        onDismissRequest = {},
        title = {
            MobileDialogTitleText(
                stringResource(Strings.pkg_manager_batch_install_title, currentIndex + 1, totalCount)
            )
        },
        text = {
            MobileDialogContentColumn {
                MobileDialogMessageCard(
                    message = stringResource(
                        Strings.pkg_manager_batch_install_msg,
                        currentPackageName,
                        platform.name
                    )
                )
                MobileDialogCard(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { (currentIndex.toFloat() + 0.5f) / totalCount },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ProgressEventContent(event)
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun BatchUpdateProgressDialog(
    currentIndex: Int,
    totalCount: Int,
    currentPackageName: String,
    event: InstallProgressEvent
) {
    MobileAlertDialog(
        onDismissRequest = {},
        title = {
            MobileDialogTitleText(
                stringResource(Strings.pkg_manager_updating_title, currentIndex + 1, totalCount)
            )
        },
        text = {
            MobileDialogContentColumn {
                MobileDialogMessageCard(
                    message = stringResource(Strings.pkg_manager_updating_msg, currentPackageName)
                )
                MobileDialogCard(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = { (currentIndex.toFloat() + 0.5f) / totalCount },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ProgressEventContent(event)
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun ProgressEventContent(event: InstallProgressEvent) {
    when (event) {
        is InstallProgressEvent.Preparing -> Text(event.message, style = MaterialTheme.typography.bodySmall)
        is InstallProgressEvent.Downloading -> {
            LinearProgressIndicator(
                progress = { event.progress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(Strings.pkg_manager_progress_downloading_simple, (event.progress * 100).toInt()), style = MaterialTheme.typography.bodySmall)
        }
        is InstallProgressEvent.Verifying -> Text(event.message, style = MaterialTheme.typography.bodySmall)
        is InstallProgressEvent.Extracting -> {
            LinearProgressIndicator(
                progress = { event.progress },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(Strings.pkg_manager_progress_extracting_simple), style = MaterialTheme.typography.bodySmall)
        }
        is InstallProgressEvent.Installing -> Text(event.message, style = MaterialTheme.typography.bodySmall)
        is InstallProgressEvent.Completed -> Text(stringResource(Strings.pkg_manager_progress_completed_simple), style = MaterialTheme.typography.bodySmall)
        is InstallProgressEvent.Failed -> Text(
            stringResource(Strings.pkg_manager_progress_failed_simple, event.error.toDisplayMessage()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}
