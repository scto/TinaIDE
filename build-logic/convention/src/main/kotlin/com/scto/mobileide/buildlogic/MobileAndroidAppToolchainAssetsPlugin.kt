package com.scto.mobileide.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Registers the `verifyMobileToolchainAssets` task and wires it into the
 * standard packaging lifecycle tasks (`assemble*`, `bundle*`, `install*`).
 *
 * The plugin intentionally keeps the task name and semantics stable for
 * downstream tooling and CI pipelines.
 */
class MobileAndroidAppToolchainAssetsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val buildAllAbiRequested = MobileToolchainAssetsVerification.resolveBuildAllAbiRequested(this)
            val localDevAbi = MobileToolchainAssetsVerification.resolveLocalDevAbi(this)
            val hostProject = this

            val verifyTask = tasks.register("verifyMobileToolchainAssets") {
                description =
                    "Verify mobile-toolchain assets referenced by current.properties exist and are consistent."
                group = "verification"
                doLast {
                    MobileToolchainAssetsVerification.verify(
                        project = hostProject,
                        logger = logger,
                        buildAllAbiRequested = buildAllAbiRequested,
                        localDevAbi = localDevAbi,
                    )
                }
            }

            // Fail-fast before packaging APK/AAB so missing assets don't
            // surface as runtime install errors.
            tasks.matching { task ->
                task.name.startsWith("assemble") ||
                    task.name.startsWith("bundle") ||
                    task.name.startsWith("install")
            }.configureEach {
                dependsOn(verifyTask)
            }
        }
    }
}
