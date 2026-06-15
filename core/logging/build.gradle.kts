plugins {
    id("mobile.android.library")
}

android {
    namespace = "com.scto.mobileide.core.logging"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:i18n"))
    implementation(project(":core:proot"))
    implementation(project(":core:storage"))
    implementation(libs.timber)
    implementation(libs.kotlinx.coroutines)
}
