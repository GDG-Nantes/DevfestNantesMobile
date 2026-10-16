# Performance Monitoring Guide

How Firebase Performance Monitoring is wired into the DevFest Nantes app on Android and iOS.

## Overview

There is no shared Kotlin abstraction for performance across platforms. Each platform calls its own
Firebase Performance SDK, and both use the same trace and attribute names so the Firebase console
shows comparable data.

| | Android | iOS |
|---|---|---|
| API | `PerformanceMonitoring` interface (`:core:analytics`, `androidMain`) | `PerformanceMonitoring` Swift class |
| Implementation | `FirebasePerformanceMonitoring` | Same class, calls `Performance.startTrace` |
| Location | `core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/performance/` | `iosApp/iosApp/Performance/PerformanceMonitoring.swift` |
| User consent | Yes (settings + first-launch dialog) | Not implemented yet |

## Trace Names

Defined in `PerformanceMonitoring.Companion` (Android) and as static constants in Swift:

| Constant | Value |
|---|---|
| `TRACE_AGENDA_LOAD` | `agenda_load` |
| `TRACE_SPEAKERS_LOAD` | `speakers_load` |
| `TRACE_SESSION_DETAILS_LOAD` | `session_details_load` |
| `TRACE_SPEAKER_DETAILS_LOAD` | `speaker_details_load` |
| `ATTR_DATA_SOURCE` | `data_source` |
| `ATTR_ERROR_TYPE` | `error_type` |

Both platforms also record an `app_startup` trace.

## Android

### API

```kotlin
interface PerformanceMonitoring {
    suspend fun <T> trace(
        traceName: String,
        attributes: Map<String, String> = emptyMap(),
        block: suspend () -> T
    ): T

    suspend fun <T> trackDataLoad(
        traceName: String,
        dataSource: String,
        itemCount: Int? = null,
        block: suspend () -> T
    ): T
}
```

`FirebasePerformanceMonitoring` implements it: it starts a Firebase trace, runs the block, records a
`duration_ms` metric and the `data_source` / `item_count` attributes, and sets `error_type` if the
block throws (the exception is rethrown).

`PerformanceExtensions.kt` adds helpers on top: `traceDataLoading(operation, dataSource)`,
`traceStateUpdate(screenName)`, `traceNetworkRequest(endpoint, method)` and
`traceComposition(componentName)`.

### Wiring

`AnalyticsPlatformModule.android.kt` binds it in Koin:

```kotlin
factory { FirebasePerformance.getInstance() }
single { FirebasePerformanceMonitoring(get()) } bind PerformanceMonitoring::class
```

ViewModels receive the interface by constructor injection. It is used by `AgendaViewModel`,
`SpeakersViewModel` and `SessionViewModel`:

```kotlin
performanceMonitoring.traceDataLoading(
    operation = PerformanceMonitoring.TRACE_SPEAKERS_LOAD,
    dataSource = "graphql"
) {
    // load data from DevFestNantesStore
}
```

### App start-up

`PerformanceInitializer` (`androidApp/.../core/performance/`) owns the `app_startup` trace:
`DevFestNantesApplication` calls `PerformanceInitializer.startAppStartupTrace()` before running the
initializers, and the initializer stops it. It runs last, after `DataSharingInitializer` has applied
the stored consent.

> **Known issue:** `PerformanceInitializer` currently sets
> `FirebasePerformance.isPerformanceCollectionEnabled = true` unconditionally, which overrides the
> value `DataSharingInitializer` just applied from the user's consent. Until this is fixed,
> performance collection is enabled at every start-up regardless of the stored choice.

### User consent

`DataCollectionSettingsService` (`feature/settings/.../services/`) stores the enabled services in
`SharedPreferences` and applies them to the SDKs:

```kotlin
enum class DataCollectionService { GOOGLE_ANALYTICS, FIREBASE_CRASHLYTICS, FIREBASE_PERFORMANCE }

firebasePerformance.isPerformanceCollectionEnabled =
    enabledDataServices.contains(DataCollectionService.FIREBASE_PERFORMANCE.name)
```

- `DataCollectionAgreementDialog` asks for consent on first launch (accept all, or customize).
- `DataCollectionSettingsScreen` lets the user toggle each service later.
- Changes apply immediately, without restarting the app.

When collection is disabled, traces are still created in code but the SDK does not send them.

### Tests

ViewModel tests use `NoOpPerformanceMonitoring` from `core/testing` (`androidMain`), which just runs
the block. Do not mock Firebase in unit tests.

## iOS

`PerformanceMonitoring.swift` is used through `PerformanceMonitoring.shared` and static helpers:

```swift
// iOSApp.swift
FirebaseApp.configure()                      // must run before any Performance API
IosDependencies.shared.start()
PerformanceMonitoring.startAppStartupTrace()
_ = RCValues.sharedInstance
PerformanceMonitoring.stopAppStartupTrace()
```

`Performance.startTrace(name:)` returns an optional; always handle `nil` instead of force-unwrapping,
and keep the explicit `import FirebasePerformance` (CI builds fail without it).

Firebase iOS SDK is added with Swift Package Manager (`firebase-ios-sdk`, up to next major from
11.14.0).

## Troubleshooting

**Instrumented tests crash with `NoSuchMethodError: registerDefaultInstance`**
Espresso brings an incompatible protobuf-lite. `androidApp/build.gradle.kts` excludes
`com.google.protobuf:protobuf-lite` from the Espresso dependencies; keep that exclusion when adding
test dependencies.

**iOS: `Failed creating trace app_startup. Firebase is not configured.`**
`FirebaseApp.configure()` must be called before `PerformanceMonitoring.startAppStartupTrace()`.

**No data in the Firebase console**
Check that Firebase Performance is enabled in the app's data-collection settings, and that you are
looking at the right app (debug builds use the `.dev` application id suffix).

## References

- [Firebase Performance Monitoring](https://firebase.google.com/docs/perf-mon)
- [ARCHITECTURE.md](ARCHITECTURE.md) - Module layout and the consent flow
