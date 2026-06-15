plugins {
    id("mobile.android.library")
}

android {
    namespace = "com.scto.mobileide.feature.output"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:i18n"))
}
