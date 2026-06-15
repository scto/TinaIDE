plugins {
    id("mobile.android.library")
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
}

android {
    namespace = "com.scto.mobileide.core.proot"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    api(project(":core:common"))
    implementation(project(":core:config"))
    implementation(project(":core:i18n"))
    implementation(project(":core:linux-distro"))
    implementation(project(":core:model"))
    implementation(project(":core:ndk"))
    implementation(project(":core:network"))
    implementation(project(":core:security"))
    implementation(project(":core:storage"))
    implementation(project(":mobile-exec:integration"))
    implementation(libs.timber)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.okhttp)
    implementation(libs.androidx.annotation)
}
