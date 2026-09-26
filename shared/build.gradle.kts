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
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:data"))
            api(project(":core:analytics"))
        }
    }
}

kotlin.targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class.java) {
    binaries.all {
        binaryOptions["memoryModel"] = "experimental"
    }
}
