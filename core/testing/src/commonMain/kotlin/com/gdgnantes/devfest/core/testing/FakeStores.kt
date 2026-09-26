package com.gdgnantes.devfest.core.testing

import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.data.DevFestNantesStoreBuilder

/**
 * Shared test-fixture entry point for `:core:*` and `:feature:*` module tests
 * (D-19). Extended in DI-05 (Phase 4) / Phase 5 as more shared fixtures are
 * needed; the existing production fakes (e.g. `DevFestNantesStoreMocked`,
 * Compose `@Preview` stubs) intentionally stay in their production modules —
 * this module re-exposes them through their public entry points rather than
 * moving them.
 *
 * Returns an in-memory [DevFestNantesStore] backed by the same mock fake used
 * in production ([DevFestNantesStoreBuilder] with mock server enabled), for
 * use in module/feature tests that need a working store without a network
 * dependency.
 */
fun fakeDevFestNantesStore(): DevFestNantesStore =
    DevFestNantesStoreBuilder().setUseMockServer(true).build()
