import com.android.build.gradle.LibraryExtension
import com.scto.mobileide.buildlogic.MobileVersions
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class MobileAndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.android")

            extensions.configure<LibraryExtension> {
                compileSdk = MobileVersions.COMPILE_SDK
                defaultConfig {
                    minSdk = MobileVersions.MIN_SDK
                    consumerProguardFiles("consumer-rules.pro")
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.toVersion(MobileVersions.JVM_TARGET)
                    targetCompatibility = JavaVersion.toVersion(MobileVersions.JVM_TARGET)
                }
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget("${MobileVersions.JVM_TARGET}"))
                }
            }

            // 统一测试依赖：所有使用 mobile.android.library 的模块自动获得基础测试库
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            dependencies {
                add("testImplementation", libs.findLibrary("junit").get())
                add("testImplementation", libs.findLibrary("tests-google-truth").get())
                add("testImplementation", libs.findLibrary("tests-robolectric").get())
                add("testImplementation", libs.findLibrary("tests-mockk").get())
                add("testImplementation", libs.findLibrary("tests-kotlinx-coroutines").get())
            }
        }
    }
}
