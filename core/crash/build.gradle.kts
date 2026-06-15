plugins {
    id("mobile.android.library")
}

android {
    namespace = "com.scto.mobileide.core.crash"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:i18n"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:storage"))
    implementation(project(":xcrash"))
    implementation(libs.timber)
    implementation(libs.kotlinx.coroutines)
}
