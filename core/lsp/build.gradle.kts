plugins {
    id("mobile.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.scto.mobileide.core.lsp"
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":core:common"))
    implementation(project(":core:config"))
    implementation(project(":core:i18n"))
    implementation(project(":core:ndk"))
    implementation(project(":core:network"))
    implementation(project(":core:packages"))
    implementation(project(":core:project"))
    implementation(project(":core:proot"))
    implementation(libs.lsp4j)
    implementation(libs.timber)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.okhttp)
    implementation(libs.androidx.annotation)
}
