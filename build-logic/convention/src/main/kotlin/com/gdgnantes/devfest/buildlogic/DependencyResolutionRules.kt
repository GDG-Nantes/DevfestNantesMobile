package com.gdgnantes.devfest.buildlogic

import org.gradle.api.Project

/**
 * Dependency-resolution rules shared by every Android module that ships
 * runtime dependencies (currently `:androidApp` only). Extracted verbatim
 * from `androidApp/build.gradle.kts`'s `configurations.configureEach` block
 * (ARCH-01) so the substitution/force stay in exactly one place.
 */
internal fun Project.configureDependencyResolutionRules() {
    configurations.configureEach {
        resolutionStrategy {
            // Kotlin 2.4.20's compiler emits @Metadata format 2.4.0, but Dagger/Hilt 2.57.2's
            // shaded room-compiler-processing bundles kotlin-metadata-jvm 2.2.20 (max readable
            // format 2.2.0), so hiltJavaCompileDebug fails with
            // "Provided Metadata instance has version 2.4.0, while maximum supported version is
            // 2.2.0." Force the reader library itself to the Kotlin version this stage bumps to;
            // kotlin-metadata-jvm's read API is designed to be forward-compatible across Kotlin
            // releases. Forced sibling of the Kotlin bump, same class as the ksp/kmpNativeCoroutines
            // bumps above — no Dagger/Hilt version change needed, and Hilt 2.59+ would force AGP 9
            // anyway (out of scope for this stage).
            force("org.jetbrains.kotlin:kotlin-metadata-jvm:${libs.findVersion("kotlin").get().requiredVersion}")
        }
        resolutionStrategy.dependencySubstitution {
            // Firebase BOM 34.19.0 dropped `firebase-auth-ktx` from its <dependencyManagement>
            // constraints (KTX extensions were merged into the main artifacts years ago; Google
            // finally stopped publishing new -ktx releases after 23.2.1). This project never
            // depends on Firebase Auth directly — `io.openfeedback:openfeedback-viewmodel` pulls
            // it in transitively via `dev.gitlive:firebase-auth`, which still requests the
            // deprecated -ktx coordinate with no explicit version, so resolution fails once the
            // BOM stops supplying one. Redirect to the plain, BOM-managed `firebase-auth`
            // artifact (same API surface, KTX extensions included) rather than pinning the
            // frozen 23.2.1 -ktx artifact, which would drag in a stale, BOM-mismatched
            // firebase-auth transitive and risk duplicate KTX extension declarations.
            // Gradle's substitution API requires an explicit target version; "24.2.0" is
            // firebase-bom 34.19.0's own managed firebase-auth version (verified against its
            // published POM), so this substitution resolves in lockstep with the BOM rather
            // than against it. Re-verify this literal alongside any future firebaseBom bump.
            substitute(module("com.google.firebase:firebase-auth-ktx"))
                .using(module("com.google.firebase:firebase-auth:24.2.0"))
                .because("firebase-bom 34.x no longer manages firebase-auth-ktx; redirect to the merged firebase-auth artifact (transitively required by openfeedback-viewmodel -> dev.gitlive:firebase-auth)")
        }
    }
}
