plugins {
    id("mobile.android.library")
}

android {
    namespace = "com.scto.mobileide.core.network"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:i18n"))
    implementation(project(":core:model"))
    implementation(project(":core:security"))
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.msgpack.core)
    implementation(libs.timber)
}
