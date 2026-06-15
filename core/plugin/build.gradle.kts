plugins {
    id("mobile.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.scto.mobileide.core.plugin"

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    api(project(":core:common"))
    api(project(":core:config"))
    implementation(project(":core:i18n"))
    implementation(project(":core:lsp"))
    implementation(project(":core:network"))
    implementation(project(":core:project"))
    implementation(project(":core:proot"))
    implementation(libs.timber)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.luajava.lua54)
}
