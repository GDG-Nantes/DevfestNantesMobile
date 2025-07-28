# Performance Monitoring Guide

This guide covers the implementation and usage of Firebase Performance Monitoring in the DevFest Nantes mobile application.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Implementation Details](#implementation-details)
- [Usage Examples](#usage-examples)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Production Deployment](#production-deployment)

## Overview

The DevFest Nantes app implements Firebase Performance Monitoring using a **platform-native approach** to ensure optimal performance and maintainability.

### Platform Support
- **Android**: Native Firebase Performance implementation with comprehensive ViewModel integration
- **iOS**: Native Firebase Performance implementation with proper optional handling
- **Architecture**: No shared abstraction layer - each platform uses native APIs directly

### Key Benefits
- **Performance**: Direct platform APIs without abstraction overhead
- **Native Features**: Platform-specific metrics (iOS app launch types, Android ANR detection)
- **Maintainability**: Simpler architecture without complex abstraction layers
- **Debugging**: Platform tools work directly with platform implementations
- **Future-Proof**: Independent platform feature adoption

## Architecture

### Platform-Native Implementation

The implementation follows a platform-native approach where each platform uses its respective Firebase Performance SDK directly:

```
┌─────────────────────────────────────────────────────────────┐
│                    Platform UI Layer                        │
│  ┌─────────────────────┐    ┌─────────────────────────────┐ │
│  │     Android App     │    │        iOS App             │ │
│  │  PerformanceMonitor │    │   PerformanceMonitoring    │ │
│  │  (Kotlin/Hilt)     │    │      (Swift)               │ │
│  └─────────────────────┘    └─────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                Firebase Performance SDKs                    │
│  ┌─────────────────────┐    ┌─────────────────────────────┐ │
│  │   Firebase Android  │    │     Firebase iOS SDK       │ │
│  │   Performance SDK   │    │    (Swift Package)         │ │
│  └─────────────────────┘    └─────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Trace Naming Convention

Both platforms use consistent trace names for unified analytics:

```kotlin
// Shared trace constants
const val TRACE_AGENDA_LOAD = "agenda_load"
const val TRACE_SPEAKERS_LOAD = "speakers_load"
const val TRACE_SESSION_DETAILS_LOAD = "session_details_load"
const val TRACE_SPEAKER_DETAILS_LOAD = "speaker_details_load"
```

## Implementation Details

### Android Implementation

#### Dependencies

Configure in `gradle/libs.versions.toml`:

```toml
[versions]
firebase-bom = "33.16.0"
firebase-performance = "2.0.0"

[libraries]
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebase-bom" }
firebase-performance = { module = "com.google.firebase:firebase-perf", version.ref = "firebase-performance" }

[plugins]
firebase-performance = { id = "com.google.firebase.firebase-perf", version.ref = "firebase-performance" }
```

#### Core Implementation

**PerformanceMonitoring.kt** - Main performance tracking class:

```kotlin
@Singleton
class PerformanceMonitoring @Inject constructor() {
    companion object {
        // Predefined trace constants
        const val TRACE_AGENDA_LOAD = "agenda_load"
        const val TRACE_SPEAKERS_LOAD = "speakers_load"
        const val TRACE_SESSION_DETAILS_LOAD = "session_details_load"
        const val TRACE_SPEAKER_DETAILS_LOAD = "speaker_details_load"
        
        // Attribute constants
        const val ATTR_DATA_SOURCE = "data_source"
        const val ATTR_ERROR_TYPE = "error_type"
    }
    
    suspend fun <T> trackDataLoad(
        traceName: String,
        dataSource: String,
        itemCount: Int? = null,
        block: suspend () -> T
    ): T {
        val trace = FirebasePerformance.getInstance().newTrace(traceName)
        trace.start()
        
        return try {
            trace.putAttribute(ATTR_DATA_SOURCE, dataSource)
            val result = block()
            itemCount?.let { trace.putMetric("item_count", it.toLong()) }
            result
        } catch (exception: Exception) {
            trace.putAttribute(ATTR_ERROR_TYPE, exception::class.simpleName ?: "Unknown")
            throw exception
        } finally {
            trace.stop()
        }
    }
}
```

**PerformanceExtensions.kt** - Convenience methods:

```kotlin
suspend fun <T> PerformanceMonitoring.traceDataLoading(
    operation: String,
    dataSource: String = "graphql",
    block: suspend () -> T
): T = trackDataLoad(operation, dataSource, block = block)

suspend fun <T> PerformanceMonitoring.trace(
    traceName: String,
    attributes: Map<String, String> = emptyMap(),
    block: suspend () -> T
): T {
    val trace = FirebasePerformance.getInstance().newTrace(traceName)
    attributes.forEach { (key, value) -> trace.putAttribute(key, value) }
    trace.start()
    
    return try {
        block()
    } finally {
        trace.stop()
    }
}
```

#### ViewModel Integration

Enhanced ViewModels with performance monitoring:

```kotlin
@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val store: DevFestNantesStore,
    private val performanceMonitoring: PerformanceMonitoring
) : ViewModel() {
    
    fun loadAgenda() {
        viewModelScope.launch {
            performanceMonitoring.traceDataLoading(
                operation = PerformanceMonitoring.TRACE_AGENDA_LOAD,
                dataSource = "graphql"
            ) {
                store.agenda.collect { agenda ->
                    // Process agenda data
                }
            }
        }
    }
}
```

### iOS Implementation

#### Dependencies

Configure Firebase iOS SDK via Swift Package Manager:
- **Package**: Firebase iOS SDK 11.15.0
- **Products**: FirebasePerformance, FirebaseCore

#### Core Implementation

**PerformanceMonitoring.swift** - Main performance tracking class:

```swift
import FirebasePerformance
import OSLog

public class PerformanceMonitoring: ObservableObject {
    private let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "Performance")
    
    // Trace constants matching Android implementation
    static let TRACE_AGENDA_LOAD = "agenda_load"
    static let TRACE_SPEAKERS_LOAD = "speakers_load"
    static let TRACE_SESSION_DETAILS_LOAD = "session_details_load"
    static let TRACE_SPEAKER_DETAILS_LOAD = "speaker_details_load"
    
    static let ATTR_DATA_SOURCE = "data_source"
    static let ATTR_ERROR_TYPE = "error_type"
    
    // App startup tracing
    public static func startAppStartupTrace() {
        guard let trace = Performance.startTrace(name: "app_startup") else {
            Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "Performance")
                .error("Failed to start app startup trace")
            return
        }
        // Store trace for later stopping
    }
    
    // Data loading tracking with proper optional handling
    func trackDataLoad<T>(
        traceName: String,
        dataSource: String = "unknown",
        operation: @escaping () async throws -> T
    ) async throws -> T {
        guard let trace = Performance.startTrace(name: traceName) else {
            logger.error("Failed to start trace: \(traceName)")
            // Graceful degradation - continue operation even if tracing fails
            return try await operation()
        }
        
        defer { trace.stop() }
        
        do {
            trace.setValue(dataSource, forAttribute: Self.ATTR_DATA_SOURCE)
            let result = try await operation()
            return result
        } catch {
            trace.setValue(String(describing: type(of: error)), forAttribute: Self.ATTR_ERROR_TYPE)
            throw error
        }
    }
    
    // Convenience method for common operations
    func traceDataLoading<T>(
        operation: String,
        dataSource: String = "graphql",
        block: @escaping () async throws -> T
    ) async throws -> T {
        return try await trackDataLoad(traceName: operation, dataSource: dataSource, operation: block)
    }
}
```

#### ViewModel Integration

Enhanced iOS ViewModels:

```swift
@MainActor
class SpeakersViewModel: BaseViewModel {
    @Published var speakers: [Speaker] = []
    @Published var isLoading = false
    
    private let performanceMonitoring = PerformanceMonitoring()
    
    func loadSpeakers() async {
        isLoading = true
        defer { isLoading = false }
        
        do {
            let speakersResult = try await performanceMonitoring.traceDataLoading(
                operation: PerformanceMonitoring.TRACE_SPEAKERS_LOAD,
                dataSource: "graphql"
            ) {
                try await asyncSequence(for: store.speakers).first { _ in true }
            }
            
            speakers = speakersResult ?? []
        } catch {
            logger.error("Failed to load speakers: \(error.localizedDescription)")
        }
    }
}
```

#### App Initialization

Configure Firebase in `iOSApp.swift`:

```swift
import SwiftUI
import FirebaseCore

@main
struct iOSApp: App {
    init() {
        // Critical: Firebase must be configured before any Performance APIs
        FirebaseApp.configure()
        PerformanceMonitoring.startAppStartupTrace()
        
        // Other initialization
        _ = RCValues.sharedInstance
        
        PerformanceMonitoring.stopAppStartupTrace()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

## Usage Examples

### Tracing Data Loading Operations

#### Android
```kotlin
// Simple data loading
performanceMonitoring.traceDataLoading(
    operation = PerformanceMonitoring.TRACE_SPEAKERS_LOAD,
    dataSource = "graphql"
) {
    store.speakers.collect { speakers ->
        // Process speakers
    }
}

// With custom attributes
performanceMonitoring.trace(
    traceName = PerformanceMonitoring.TRACE_SESSION_DETAILS_LOAD,
    attributes = mapOf(
        PerformanceMonitoring.ATTR_DATA_SOURCE to "graphql",
        "session_id" to sessionId
    )
) {
    store.getSession(sessionId)
}
```

#### iOS
```swift
// Simple data loading
let speakers = try await performanceMonitoring.traceDataLoading(
    operation: PerformanceMonitoring.TRACE_SPEAKERS_LOAD,
    dataSource: "graphql"
) {
    try await asyncFunction(for: store.speakers)
}

// Custom trace with attributes
let session = try await performanceMonitoring.trackDataLoad(
    traceName: PerformanceMonitoring.TRACE_SESSION_DETAILS_LOAD,
    dataSource: "graphql"
) {
    try await asyncFunction(for: store.getSession(id: sessionId))
}
```

### Custom Traces

#### Android
```kotlin
// Network request tracing
performanceMonitoring.trace(
    traceName = "api_request",
    attributes = mapOf(
        "endpoint" to "/sessions",
        "method" to "GET"
    )
) {
    apiClient.getSessions()
}
```

#### iOS
```swift
// Screen navigation tracing
func trackNavigation(to screen: String) {
    guard let trace = Performance.startTrace(name: "screen_navigation") else { return }
    trace.setValue(screen, forAttribute: "destination")
    // Stop trace when navigation completes
    trace.stop()
}
```

## Testing

### Running Tests

Both unit tests and instrumented tests should pass:

```bash
# Android unit tests
./gradlew :androidApp:testDebugUnitTest

# Android instrumented tests
./gradlew :androidApp:connectedDebugAndroidTest

# iOS tests (requires Xcode)
xcodebuild test -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 16 Pro,OS=18.3.1'
```

### Test Environment Configuration

For testing, you can disable Firebase Performance or use mock implementations:

```kotlin
// Android - Test configuration
@Provides
@Singleton
fun providePerformanceMonitoring(): PerformanceMonitoring {
    return if (BuildConfig.DEBUG) {
        MockPerformanceMonitoring()
    } else {
        PerformanceMonitoring()
    }
}
```

## Troubleshooting

### Common Issues

#### Android Test Failures - Protobuf Dependency Conflicts

**Issue**: AndroidTest fails with `NoSuchMethodError: No static method registerDefaultInstance` during Firebase Performance initialization.

**Root Cause**: Conflicting protobuf versions between Firebase Performance and Espresso test dependencies.

**Solution**: Exclude protobuf-lite from Espresso dependencies in `androidApp/build.gradle.kts`:

```kotlin
androidTestImplementation(libs.androidx.test.espresso) {
    exclude(group = "com.google.protobuf", module = "protobuf-lite")
}
androidTestImplementation(libs.androidx.test.espresso.contrib) {
    exclude(group = "com.google.protobuf", module = "protobuf-lite")
}
androidTestImplementation(libs.androidx.test.espresso.intents) {
    exclude(group = "com.google.protobuf", module = "protobuf-lite")
}
```

**Why this works**: Firebase BOM manages the correct protobuf version for Firebase Performance, while Espresso brings in an incompatible version. Excluding protobuf from Espresso allows Firebase's version to be used consistently.

#### iOS Firebase Configuration Issues

**Issue**: App crashes with "Firebase is not configured" error.

**Error Message**: `[FirebasePerformance][I-PRF200007] Failed creating trace app_startup. Firebase is not configured.`

**Solution**: Ensure `FirebaseApp.configure()` is called **before** any Firebase Performance APIs:

```swift
// ❌ WRONG - Will crash
init() {
    PerformanceMonitoring.startAppStartupTrace() // Called before Firebase is configured
    FirebaseApp.configure()
}

// ✅ CORRECT - Firebase configured first
init() {
    FirebaseApp.configure() // Configure Firebase first
    PerformanceMonitoring.startAppStartupTrace() // Now safe to use Firebase Performance
}
```

#### iOS Optional Trace Handling

**Issue**: Crashes when `Performance.startTrace()` returns nil.

**Solution**: Always handle optional Trace values:

```swift
// ❌ WRONG - Force unwrapping
let trace = Performance.startTrace(name: "my_trace")!

// ✅ CORRECT - Proper optional handling
guard let trace = Performance.startTrace(name: "my_trace") else {
    logger.error("Failed to start trace")
    return // or continue without tracing
}
```

### Build Issues

#### KSP Version Compatibility (Android)
Ensure Kotlin and KSP versions are aligned in `gradle/libs.versions.toml`:

```toml
kotlin = "2.2.0"
ksp = "2.2.0-2.0.2"  # Should match Kotlin version
```

#### Swift Package Manager Issues (iOS)
If you encounter SPM build failures:

1. Clean build folder: Product > Clean Build Folder
2. Reset package caches: File > Packages > Reset Package Caches
3. Update packages: File > Packages > Update to Latest Package Versions

## Production Deployment

### Pre-Production Checklist

- [ ] **Performance Data Collection**: Verify data appears in Firebase Performance Console
- [ ] **Platform Testing**: Test on physical devices for both Android and iOS
- [ ] **Metrics Validation**: Confirm custom traces and metrics are recorded correctly
- [ ] **Load Testing**: Validate performance under various app usage scenarios
- [ ] **Error Handling**: Verify app continues to function when Firebase Performance fails

### Monitoring Setup

1. **Dashboard Configuration**: Set up Firebase Performance Console dashboards
2. **Alert Configuration**: Configure performance degradation alerts
3. **Baseline Establishment**: Define acceptable performance baselines
4. **Review Process**: Establish regular performance monitoring reviews

### Performance Metrics to Monitor

- **App Startup Time**: Track cold and warm startup performance
- **Screen Load Times**: Monitor navigation and data loading performance
- **Network Requests**: Track API response times and failure rates
- **Memory Usage**: Monitor memory consumption patterns
- **Crash-Free Sessions**: Correlate performance with app stability

### Gradual Rollout Strategy

1. **Staging Environment**: Validate in staging with production-like data
2. **Beta Testing**: Deploy to beta users for real-world validation
3. **Canary Release**: Gradual rollout to percentage of production users
4. **Full Deployment**: Complete rollout with continuous monitoring

## References

### Firebase Documentation
- [Firebase Performance Monitoring](https://firebase.google.com/docs/perf-mon)
- [Android Firebase Performance Guide](https://firebase.google.com/docs/perf-mon/get-started-android)
- [iOS Firebase Performance Guide](https://firebase.google.com/docs/perf-mon/get-started-ios)

### Project Documentation
- [Architecture Guide](ARCHITECTURE.md) - Overall app architecture
- [Development Setup](DEVELOPMENT.md) - Development environment setup
- [Contributing Guidelines](../CONTRIBUTING.md) - Contribution workflow

---

*Last updated: July 28, 2025*
*Implementation: Platform-Native Firebase Performance Monitoring*
*Platforms: Android (Kotlin) + iOS (Swift)*
