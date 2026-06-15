plugins {
    id("mobile.android.library")
}

android {
    namespace = "com.scto.mobileide.core.config"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:i18n"))
    implementation(project(":core:network"))
    implementation(libs.kotlinx.coroutines)
    implementation(libs.androidx.appcompat)
    implementation(libs.timber)
}
