package com.scto.mobileide.buildlogic

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Registers the release R8 mapping backup/upload pipeline for the
 * application module:
 *
 * - `backupMappingFiles` copies `build/outputs/mapping/<flavor>Release/mapping.txt`
 *   into `app/mappings/<versionName>-<timestamp>/<flavor>Release/mapping.txt`.
 * - `uploadMappingFiles` optionally gzip-compresses each mapping file and
 *   POSTs it to the configured server endpoint so that crash reports can be
 *   de-obfuscated server-side.
 *
 * Backup is enabled by default for release builds. Upload is opt-in so the
 * open-source project does not contact private backend services unless a
 * maintainer explicitly enables it.
 *
 * Configuration properties:
 * - `mobile.releaseMapping.enabled` (default `true`): toggle the whole
 *   pipeline; when `false`, both tasks become no-ops and no finalizer
 *   is attached.
 * - `mobile.releaseMapping.backupEnabled` (default `true`): attach
 *   `backupMappingFiles` to release builds.
 * - `mobile.releaseMapping.uploadEnabled` (default `false`): attach
 *   `uploadMappingFiles` to release builds.
 * - `mobile.releaseMapping.serverUrl` (default
 *   `https://mobileide.Thomas Schmid.com`): upload target.
 *
 * Requires the consumer project to have applied
 * [MobileAndroidAppVersioningPlugin] because it reads version information
 * from the [MobileAppVersioningExtension] exposed by that plugin.
 */
class MobileAndroidAppMappingPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            val enabled = resolveBooleanGradleProperty(
                name = "mobile.releaseMapping.enabled",
                default = true,
            )
            val backupEnabled = resolveBooleanGradleProperty(
                name = "mobile.releaseMapping.backupEnabled",
                default = true,
            )
            val uploadEnabled = resolveBooleanGradleProperty(
                name = "mobile.releaseMapping.uploadEnabled",
                default = false,
            )
            val serverUrl = providers.gradleProperty("mobile.releaseMapping.serverUrl")
                .orNull
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: DEFAULT_SERVER_URL

            // 两个任务都保持注册（即使当前被 disable），
            // 这样用户可以手动 `./gradlew :app:backupMappingFiles` 做一次性归档。
            tasks.register("backupMappingFiles") {
                group = "build"
                description = "Backup R8 mapping files for crash log deobfuscation."
                onlyIf { enabled && backupEnabled }

                doLast {
                    val versionName = project.readAppVersionName()
                    MobileMappingFileBackup.backupMappings(
                        mappingRoot = project.file("build/outputs/mapping"),
                        backupsRoot = project.file("mappings"),
                        versionName = versionName,
                        logger = logger,
                    )
                }
            }

            tasks.register("uploadMappingFiles") {
                group = "build"
                description = "Upload R8 mapping files to server for crash log deobfuscation."
                onlyIf { enabled && uploadEnabled }

                doLast {
                    val versionName = project.readAppVersionName()
                    val versionCode = project.readAppVersionCode()
                    MobileMappingFileUpload.uploadMappings(
                        mappingRoot = project.file("build/outputs/mapping"),
                        serverUrl = serverUrl,
                        appVersionName = versionName,
                        appVersionCode = versionCode,
                        logger = logger,
                    )
                }
            }

            if (!enabled) {
                logger.lifecycle(
                    "mobile.android.app.mapping: mobile.releaseMapping.enabled=false, " +
                        "skipping finalizedBy wiring for release assemble/bundle tasks.",
                )
                return@with
            }

            afterEvaluate {
                val finalizers = buildList {
                    if (backupEnabled) add("backupMappingFiles")
                    if (uploadEnabled) add("uploadMappingFiles")
                }
                if (finalizers.isEmpty()) {
                    logger.lifecycle(
                        "mobile.android.app.mapping: backup/upload finalizers disabled for release tasks.",
                    )
                    return@afterEvaluate
                }
                tasks.matching { it.name.matches(Regex("assemble.*Release")) }.configureEach {
                    finalizers.forEach { finalizedBy(it) }
                }
                tasks.matching { it.name.matches(Regex("bundle.*Release")) }.configureEach {
                    finalizers.forEach { finalizedBy(it) }
                }
            }
        }
    }

    private fun Project.readAppVersionName(): String {
        return readVersioningExtension().versionName
    }

    private fun Project.readAppVersionCode(): Int {
        return readVersioningExtension().versionCode
    }

    private fun Project.readVersioningExtension(): MobileAppVersioningExtension {
        return extensions.findByType(MobileAppVersioningExtension::class.java)
            ?: throw GradleException(
                "mobile.android.app.mapping requires mobile.android.app.versioning to be applied " +
                    "first so that MobileAppVersioningExtension is available.",
            )
    }

    companion object {
        private const val DEFAULT_SERVER_URL = "https://mobileide.Thomas Schmid.com"
    }
}
