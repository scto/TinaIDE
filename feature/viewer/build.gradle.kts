plugins {
    id("mobile.android.library")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.scto.mobileide.feature.viewer"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:i18n"))
    implementation(libs.kotlinx.coroutines)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui)
    implementation(libs.coil.compose)
}
