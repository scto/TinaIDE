package com.scto.mobileide.terminal.install

import android.content.Context
import com.scto.mobileide.core.i18n.Strings
import com.scto.mobileide.core.i18n.strOr
import com.scto.mobileide.core.linux.LinuxEnvironment
import com.scto.mobileide.core.linux.LinuxEnvironmentProvider
import com.scto.mobileide.core.linux.UnavailableLinuxEnvironmentProvider
import com.scto.mobileide.core.proot.GuestSystemPackageManager
import com.scto.mobileide.core.proot.RootfsPackageManager
import com.scto.mobileide.core.proot.displayName
import com.scto.mobileide.core.proot.resolveGuestPackageManager
import com.scto.mobileide.core.terminal.GuestDevPackagesCommandGroupStatus
import com.scto.mobileide.core.terminal.GuestDevPackagesInstallResult
import com.scto.mobileide.core.terminal.GuestDevPackagesStatus
import com.scto.mobileide.core.terminal.IGuestDevPackagesInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GuestDevelopmentPackagesInstaller(
    private val context: Context,
    private val linuxEnvironmentProvider: LinuxEnvironmentProvider = runCatching {
        org.koin.core.context.GlobalContext.get().getOrNull<LinuxEnvironmentProvider>()
    }.getOrNull() ?: UnavailableLinuxEnvironmentProvider,
) : IGuestDevPackagesInstaller {

    override suspend fun inspectStatus(): GuestDevPackagesStatus = withContext(Dispatchers.IO) {
        val linuxEnvironment = linuxEnvironmentProvider.get()
        if (!linuxEnvironment.isAvailable()) {
            return@withContext GuestDevPackagesStatus(
                installed = false,
                plannedPackages = emptyList(),
                commandGroupStatuses = emptyList(),
            )
        }

        val packageManager = linuxEnvironment.resolveGuestPackageManager()
        val plan = buildDevelopmentPackagesPlan(packageManager)
            ?: return@withContext GuestDevPackagesStatus(
                installed = false,
                plannedPackages = emptyList(),
                commandGroupStatuses = emptyList(),
            )
        val missingCommandGroups = findMissingDevelopmentCommandGroups(
            linuxEnvironment = linuxEnvironment,
            plan = plan,
        )
        val commandGroupStatuses = plan.commandGroups.map { group ->
            GuestDevPackagesCommandGroupStatus(
                commands = group,
                available = group !in missingCommandGroups,
            )
        }
        GuestDevPackagesStatus(
            installed = missingCommandGroups.isEmpty(),
            plannedPackages = plan.packages,
            commandGroupStatuses = commandGroupStatuses,
        )
    }

    override suspend fun isInstalled(): Boolean = withContext(Dispatchers.IO) {
        inspectStatus().installed
    }

    override suspend fun install(
        force: Boolean,
        onProgress: (String) -> Unit
    ): GuestDevPackagesInstallResult = withContext(Dispatchers.IO) {
        val linuxEnvironment = linuxEnvironmentProvider.get()
        if (!linuxEnvironment.isAvailable()) {
            return@withContext GuestDevPackagesInstallResult.Error(
                Strings.dev_packages_error_proot_not_installed.strOr(context)
            )
        }

        try {
            val packageManager = linuxEnvironment.resolveGuestPackageManager()
            val plan = buildDevelopmentPackagesPlan(packageManager)
                ?: return@withContext GuestDevPackagesInstallResult.Error(
                Strings.dev_packages_error_unsupported_package_manager.strOr(
                        context,
                        packageManager.displayName()
                    )
                )

            if (!force && isInstalled()) {
                return@withContext GuestDevPackagesInstallResult.Success
            }

            onProgress(Strings.dev_packages_updating_index.strOr(context))
            val updateResult = GuestSystemPackageManager.updateIndex(
                linuxEnvironment = linuxEnvironment,
                packageManager = packageManager,
                timeoutMs = 120_000,
            )
            if (updateResult.exitCode != 0) {
                return@withContext GuestDevPackagesInstallResult.Error(
                    Strings.dev_packages_error_update_failed.strOr(
                        context,
                        updateResult.combinedOutput.ifBlank { "${packageManager.displayName()} update failed" }
                    )
                )
            }

            onProgress(Strings.dev_packages_installing.strOr(context))
            val installResult = GuestSystemPackageManager.installPackages(
                linuxEnvironment = linuxEnvironment,
                packageManager = packageManager,
                packages = plan.packages,
                timeoutMs = 600_000,
                force = force,
            )
            if (installResult.exitCode != 0) {
                return@withContext GuestDevPackagesInstallResult.Error(
                    Strings.dev_packages_error_install_failed.strOr(
                        context,
                        installResult.combinedOutput.ifBlank {
                            "${packageManager.displayName()} install ${plan.packages.joinToString(" ")} failed"
                        }
                    )
                )
            }

            onProgress(Strings.dev_packages_verifying.strOr(context))
            if (findMissingDevelopmentCommandGroups(linuxEnvironment, plan).isEmpty()) {
                GuestDevPackagesInstallResult.Success
            } else {
                GuestDevPackagesInstallResult.Error(
                    Strings.dev_packages_error_verify_failed.strOr(context)
                )
            }
        } catch (e: Exception) {
            GuestDevPackagesInstallResult.Error(
                Strings.dev_packages_error_exception.strOr(
                    context,
                    e.message ?: Strings.error_unknown.strOr(context)
                )
            )
        }
    }

    private suspend fun findMissingDevelopmentCommandGroups(
        linuxEnvironment: LinuxEnvironment,
        plan: DevelopmentPackagesPlan,
    ): List<List<String>> {
        return GuestSystemPackageManager.findMissingCommandGroups(
            linuxEnvironment = linuxEnvironment,
            commandGroups = plan.commandGroups,
        )
    }
}

internal data class DevelopmentPackagesPlan(
    val packages: List<String>,
    val commandGroups: List<List<String>>,
)

internal fun buildDevelopmentPackagesPlan(
    packageManager: RootfsPackageManager,
): DevelopmentPackagesPlan? {
    return when (packageManager) {
        RootfsPackageManager.APK -> DevelopmentPackagesPlan(
            packages = listOf("build-base", "git", "curl", "pkgconf", "cmake"),
            commandGroups = buildDevelopmentCommandGroups(),
        )
        RootfsPackageManager.APT -> DevelopmentPackagesPlan(
            packages = listOf("build-essential", "git", "curl", "pkg-config", "cmake"),
            commandGroups = buildDevelopmentCommandGroups(),
        )
        RootfsPackageManager.PACMAN -> DevelopmentPackagesPlan(
            packages = listOf("base-devel", "git", "curl", "pkgconf", "cmake"),
            commandGroups = buildDevelopmentCommandGroups(),
        )
        RootfsPackageManager.DNF -> DevelopmentPackagesPlan(
            packages = listOf("gcc", "gcc-c++", "make", "git", "curl", "pkgconf-pkg-config", "cmake"),
            commandGroups = buildDevelopmentCommandGroups(),
        )
        RootfsPackageManager.UNKNOWN -> null
    }
}

private fun buildDevelopmentCommandGroups(): List<List<String>> {
    return listOf(
        listOf("cc", "gcc", "clang"),
        listOf("make"),
        listOf("git"),
        listOf("curl"),
        listOf("cmake"),
        listOf("pkg-config", "pkgconf"),
    )
}
