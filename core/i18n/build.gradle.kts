plugins {
    id("mobile.android.library")
}

android {
    namespace = "com.scto.mobileide.core.i18n"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.annotation)
}
