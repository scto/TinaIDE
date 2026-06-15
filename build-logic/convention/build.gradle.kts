plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.plugins.android.application.toDep())
    compileOnly(libs.plugins.android.library.toDep())
    compileOnly(libs.plugins.kotlin.android.toDep())
    compileOnly(libs.plugins.compose.compiler.toDep())
}

fun Provider<PluginDependency>.toDep() = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "mobile.android.library"
            implementationClass = "MobileAndroidLibraryPlugin"
        }
        register("androidLibraryCompose") {
            id = "mobile.android.library.compose"
            implementationClass = "MobileAndroidLibraryComposePlugin"
        }
        register("androidAppVersioning") {
            id = "mobile.android.app.versioning"
            implementationClass = "com.scto.mobileide.buildlogic.MobileAndroidAppVersioningPlugin"
        }
        register("androidAppToolchainAssets") {
            id = "mobile.android.app.toolchain.assets"
            implementationClass = "com.scto.mobileide.buildlogic.MobileAndroidAppToolchainAssetsPlugin"
        }
        register("androidAppAbiAggregation") {
            id = "mobile.android.app.abi-aggregation"
            implementationClass = "com.scto.mobileide.buildlogic.MobileAndroidAppAbiAggregationPlugin"
        }
        register("androidAppGuardrails") {
            id = "mobile.android.app.guardrails"
            implementationClass = "com.scto.mobileide.buildlogic.MobileAndroidAppGuardrailsPlugin"
        }
        register("androidAppTreeSitter") {
            id = "mobile.android.app.treesitter"
            implementationClass = "com.scto.mobileide.buildlogic.MobileAndroidAppTreeSitterPlugin"
        }
        register("androidAppMapping") {
            id = "mobile.android.app.mapping"
            implementationClass = "com.scto.mobileide.buildlogic.MobileAndroidAppMappingPlugin"
        }
    }
}
