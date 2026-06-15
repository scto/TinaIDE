plugins {
    id("mobile.android.library")
}

android {
    namespace = "com.scto.mobileide.exec.integration"
}

dependencies {
    implementation(project(":mobile-exec:runtime"))
}
