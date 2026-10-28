plugins {
    id("devfest.kmp.library")
}

kotlin {
    targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class.java) {
        binaries.framework {
            baseName = "shared"
            isStatic = true
            export(project(":core:model"))
            export(project(":core:data"))
            export(project(":core:analytics"))
            export(project(":core:domain"))
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:data"))
            api(project(":core:analytics"))
            api(project(":core:domain"))
            implementation(project(":core:network"))
            implementation(libs.koin.core)
        }
        jvmTest.dependencies {
            implementation(libs.koin.test)
        }
    }
}

kotlin.targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class.java) {
    binaries.all {
        binaryOptions["memoryModel"] = "experimental"
    }
}
