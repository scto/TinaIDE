plugins {
    id("mobile.android.library")
}

android {
    namespace = "com.scto.mobileide.core.search"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.kotlinx.coroutines)
}
