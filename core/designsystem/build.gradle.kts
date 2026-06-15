plugins {
    id("mobile.android.library")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.scto.mobileide.core.designsystem"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:config"))
    implementation(project(":core:i18n"))
    implementation(libs.activity.compose)
    implementation(libs.kotlinx.coroutines)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui)
    implementation(libs.jetbrains.markdown)
    implementation(libs.timber)
}
