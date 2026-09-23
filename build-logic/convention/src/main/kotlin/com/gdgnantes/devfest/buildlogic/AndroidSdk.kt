package com.gdgnantes.devfest.buildlogic

object AndroidSdk {
    const val min = 26
    const val compile = 37
    // Deliberately NOT coupled to `compile` anymore: Compose BOM 2026.09.00's AAR
    // metadata requires compileSdk >= 37 (compile-time API surface only), but
    // bumping targetSdk changes runtime behavior for all users on all OS versions
    // (opts the app into Android 17 behavior changes) and is out of this
    // build-tooling-only phase's scope per PROJECT.md's no-regression constraint.
    // Kept at the pre-existing value.
    const val target = 36
}
