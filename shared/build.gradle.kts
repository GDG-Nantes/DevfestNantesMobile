import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version Versions.kotlinVersion
    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.rickclephas.kmp.nativecoroutines") version Versions.kmpNativeCoroutines
    id("com.apollographql.apollo3") version Apollo.apolloVersion
}

version = "1.0"

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                with(Kotlinx) {
                    implementation(dateTime)
                    implementation(serialization)
                }

                with(Apollo) {
                    implementation(apolloRuntime)
                    implementation(apolloNormalizedCache)
                    implementation(apolloNormalizedCacheSqlite)
                }
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting
        val androidUnitTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

kotlin.targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class.java) {
    binaries.all {
        binaryOptions["memoryModel"] = "experimental"
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
}

apollo {
    service("service") {
        packageName.set("com.gdgnantes.devfest.graphql")
    }
}

tasks.withType(JavaCompile::class.java).configureEach {
    options.release.set(17)
}
tasks.withType(KotlinCompile::class.java).configureEach {
    (kotlinOptions as? KotlinJvmOptions)?.jvmTarget = "17"
}

android {
    compileSdk = AndroidSdk.compile
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 23
    }
    namespace = "com.gdgnantes.devfest"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}