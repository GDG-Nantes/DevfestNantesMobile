# Phase 4: Hilt to Koin DI Migration - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-09-28
**Phase:** 04-hilt-to-koin-di-migration
**Areas discussed:** iOS & initKoin(), Migration sequencing, Parameterized ViewModels, Graph verification in CI

---

## iOS & initKoin()

| Option | Description | Selected |
|--------|-------------|----------|
| Yes, Swift calls initKoin() | iOS App init calls initKoin(); Swift gets the store via a Kotlin helper | ✓ |
| No, iOS keeps the builder | Koin Android-only; Swift keeps DevFestNantesStoreBuilder | |
| Koin on iOS, no Swift changes yet | initKoin() exposed and tested in iosTest; Swift call sites later | |

| Option | Description | Selected |
|--------|-------------|----------|
| One shared singleton | One Apollo client + normalized cache on iOS, as on Android | ✓ |
| Keep one store per screen | Koin factory; exact current behavior | |

| Option | Description | Selected |
|--------|-------------|----------|
| In :shared (umbrella) | commonMain initKoin(platformModules) + iosMain helper; amends D-10 | ✓ |
| New :core:di module | Dedicated aggregator module | |
| Separate roots per platform | No common initKoin() | |

| Option | Description | Selected |
|--------|-------------|----------|
| Typed Kotlin helper | iosMain IosDependencies with typed getters; BaseViewModel default init arg | ✓ |
| Generic Swift inject<T>() | ObjC class-lookup based generic resolver | |

**User's choice:** All recommended options.

---

## Migration sequencing

| Option | Description | Selected |
|--------|-------------|----------|
| Tracer, then bottom-up coexistence | Koin + initKoin + venue tracer; per-module commits; Hilt removed last | ✓ |
| Koin-first, then remove Hilt in one go | Parallel graph, one big cutover | |
| Big-bang swap | Single change | |

| Option | Description | Selected |
|--------|-------------|----------|
| Koin owns them, Hilt delegates | AppModule @Provides become temporary Koin get() bridges | ✓ |
| Hilt owns them, Koin delegates | Koin pulls from Hilt EntryPoint until the end | |

| Option | Description | Selected |
|--------|-------------|----------|
| Owning module's androidMain | expect/actual platform modules per core module; app keeps BuildConfig-bound bindings | ✓ |
| Keep all of it in :androidApp | Single app-level Android module | |

| Option | Description | Selected |
|--------|-------------|----------|
| Inside the tracer step | iOS switch + simulator smoke in tracer | ✓ |
| At the end, after Android | iOS switched in one final step | |

**User's choice:** All recommended options.

---

## Parameterized ViewModels

| Option | Description | Selected |
|--------|-------------|----------|
| Koin parametersOf(id) | Plain constructor param; removes AssistedInject/EntryPoint/utils | ✓ |
| SavedStateHandle nav args | ViewModel reads the route arg | |

| Option | Description | Selected |
|--------|-------------|----------|
| Inside the feature Route | Route takes id, resolves koinViewModel { parametersOf(id) } | ✓ |
| App module creates it | MainActivity resolves and passes the ViewModel | |

| Option | Description | Selected |
|--------|-------------|----------|
| by inject() in Android entry points only | Application/Activity may inject; initializers via getAll | ✓ |
| Constructor-only, even the Activity | AppGraph object passed down | |

**User's choice:** All recommended options.

---

## Graph verification in CI

| Option | Description | Selected |
|--------|-------------|----------|
| Static verify() + Android start-up test | JVM verify() plus a runtime graph start/resolve test | ✓ |
| Static verify() only | Cheapest | |
| Runtime checkModules() as written | Deprecated API | |

| Option | Description | Selected |
|--------|-------------|----------|
| Per-module + one full-graph test | Per-module verify, :androidApp full graph, iOS initKoin test | ✓ |
| Only the full-graph test in :androidApp | Single test | |

| Option | Description | Selected |
|--------|-------------|----------|
| DI-focused fakes only | FakeBookmarksStore, FakeAnalyticsService, no-op PerformanceMonitoring, Koin test helper | ✓ |
| Minimal: Koin helper only | | |
| Fakes for every interface | | |

| Option | Description | Selected |
|--------|-------------|----------|
| Add a scripted gate | CI script failing on Hilt/Dagger refs and get()/inject() outside wiring | ✓ |
| One-time verification only | Verifier grep at phase end | |

**User's choice:** All recommended options.

---

## Claude's Discretion

- Koin version/BOM pin and artifact set
- DSL style, module naming, file placement
- expect/actual platform module shape
- Convention-plugin changes and Hilt removal order in build-logic
- Android/iOS manual smoke checkpoints (minimum: after tracer and after final Hilt removal)
- CoroutinesDispatcherProvider placement
- When the CI gate is introduced (early with shrinking allowlist vs end)

## Deferred Ideas

None raised.
