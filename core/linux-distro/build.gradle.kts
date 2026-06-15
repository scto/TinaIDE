plugins {
    id("mobile.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.scto.mobileide.core.linuxdistro"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
}