plugins {
    id("mobile.android.library")
}

android {
    namespace = "com.scto.mobileide.core.security"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:i18n"))
    implementation(project(":core:storage"))
}
