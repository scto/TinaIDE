plugins {
    id("mobile.android.library")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.scto.mobileide.feature.projectlist"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:i18n"))
    implementation(project(":core:project"))
    implementation(project(":core:storage"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui)
}
