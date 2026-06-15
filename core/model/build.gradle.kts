plugins {
    id("mobile.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.scto.mobileide.core.model"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
