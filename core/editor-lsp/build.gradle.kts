plugins {
    id("mobile.android.library")
}

android {
    namespace = "com.scto.mobileide.core.editorlsp"
}

dependencies {
    implementation(project(":core:text-engine"))
    implementation(project(":core:lsp"))
    implementation(project(":core:common"))

    implementation(libs.kotlinx.coroutines)
    implementation(libs.timber)
    api(libs.lsp4j)
}

