plugins {
    id("mobile.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.scto.mobileide.core.project"
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:i18n"))
    implementation(libs.timber)
    implementation(libs.kotlinx.serialization.json)
}
