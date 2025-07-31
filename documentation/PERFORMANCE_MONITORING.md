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

The DevFest Nantes app implements Firebase Performance Monitoring using a **platform-native approach** with **user consent management** to ensure optimal performance, maintainability, and privacy compliance.

### Platform Support
- **Android**: Native Firebase Performance implementation with comprehensive ViewModel integration and user consent system
- **iOS**: Native Firebase Performance implementation with proper optional handling (consent system planned)
- **Architecture**: No shared abstraction layer - each platform uses native APIs directly
- **Privacy**: User consent required before enabling performance monitoring

### Key Benefits
- **Performance**: Direct platform APIs without abstraction overhead
- **Native Features**: Platform-specific metrics (iOS app launch types, Android ANR detection)
- **Maintainability**: Simpler architecture without complex abstraction layers
- **Debugging**: Platform tools work directly with platform implementations
- **Future-Proof**: Independent platform feature adoption
- **Privacy Compliant**: User consent management ensures GDPR/privacy regulation compliance

### User Consent System (Android)

The Android app implements a comprehensive consent management system that allows users to control what data they share:

**Available Services:**
- **Google Analytics**: User interaction and app usage analytics
- **Firebase Crashlytics**: Crash reporting and stability monitoring
- **Firebase Performance Monitoring**: App performance and network monitoring

**Consent Features:**
- Initial consent dialog on first app launch
- Granular control over each service in settings
- Ability to consent to all or customize individual services
- Settings can be changed at any time from the app settings
- Performance monitoring is disabled by default until user consent is given

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

#### User Consent System

**DataCollectionSettingsService** - Manages user consent for all Firebase services:

```kotlin
interface DataCollectionSettingsService {
    val isDataCollectionAgreementSet: Boolean
    val dataCollectionServicesActivationStatus: Flow<Map<DataCollectionService, Boolean>>
    
    fun changeDataServiceActivationStatus(
        dataCollectionService: DataCollectionService,
        enabled: Boolean
    )
    fun consentToAllServices()
    fun disallowAllServices()
}

enum class DataCollectionService {
    GOOGLE_ANALYTICS,
    FIREBASE_CRASHLYTICS,
    FIREBASE_PERFORMANCE,
}
```

**Consent Flow Implementation:**

```kotlin
@Singleton
class DataCollectionSettingsServiceImpl @Inject constructor(
    private val firebasePerformance: FirebasePerformance,
    // other Firebase services...
) : DataCollectionSettingsService {
    
    private fun updatesFirebasePerformanceActivationStatus() {
        firebasePerformance.isPerformanceCollectionEnabled =
            enabledDataServices.contains(DataCollectionService.FIREBASE_PERFORMANCE.name)
    }
    
    override fun consentToAllServices() {
        DataCollectionService.values().map { it.name }.toSet().let {
            enabledDataServices.addAll(it)
        }
        synchronise()
    }
}
```

**Consent UI Components:**

- **DataCollectionAgreementDialog**: Initial consent dialog shown on first app launch
- **DataCollectionViewModel**: Manages consent state and user interactions
- **Settings Screen**: Allows users to modify consent preferences anytime

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

**PerformanceMonitoring.kt** - Main performance tracking class (respects user consent):

```kotlin
@Singleton
class PerformanceMonitoring @Inject constructor(
    private val firebasePerformance: FirebasePerformance
) {
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
    
    /**
     * Tracks data loading performance.
     * Only collects data if user has consented to Firebase Performance.
     */
    suspend fun <T> trackDataLoad(
        traceName: String,
        dataSource: String,
        itemCount: Int? = null,
        block: suspend () -> T
    ): T {
        // Performance monitoring automatically respects user consent
        // via firebasePerformance.isPerformanceCollectionEnabled
        val trace = firebasePerformance.newTrace(traceName)
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

**Key Features:**
- **Automatic Consent Respect**: Firebase Performance SDK automatically checks `isPerformanceCollectionEnabled`
- **No Data Collection Without Consent**: When disabled, traces are created but no data is sent to Firebase
- **Graceful Degradation**: App functionality continues normally regardless of consent status
- **Real-time Updates**: Consent changes take effect immediately without app restart
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

#### User Consent System (In Development)

The iOS app includes localized strings for consent management, indicating planned implementation:

**Localized Consent Strings:**
- `legal_data_collection_consent_dialog_body`: "We use third party tools to measure and improve performances..."
- `legal_data_collection_consent_dialog_button_consent`: "Consent"
- `button_dialog_data_collection_consent_customize`: "Customize"

**Future Implementation Notes:**
- iOS consent system to match Android functionality
- Will use native iOS APIs for preference management
- Firebase Performance SDK supports `isPerformanceCollectionEnabled` property
- Consent state should be synchronized across app launches

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
    
    /**
     * Tracks data loading performance.
     * Respects user consent when implemented - Firebase Performance SDK
     * automatically handles consent via Performance.isDataCollectionEnabled
     */
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
}
```

**Key Features:**
- **Future Consent Integration**: Ready for consent system implementation
- **Graceful Degradation**: App continues working if Performance monitoring fails
- **Consistent Naming**: Matches Android trace and attribute names
- **Proper Error Handling**: Logs failures but doesn't crash the app
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

### User Consent Management (Android)

#### Checking Consent Status

```kotlin
@HiltViewModel
class ExampleViewModel @Inject constructor(
    private val dataCollectionSettingsService: DataCollectionSettingsService,
    private val performanceMonitoring: PerformanceMonitoring
) : ViewModel() {
    
    fun checkPerformanceConsentStatus() {
        dataCollectionSettingsService.dataCollectionServicesActivationStatus
            .map { statusMap -> 
                statusMap[DataCollectionService.FIREBASE_PERFORMANCE] ?: false
            }
            .collect { isPerformanceEnabled ->
                // Performance monitoring will automatically respect this setting
                // No additional checks needed in performance monitoring calls
            }
    }
}
```

#### Managing User Consent

```kotlin
@HiltViewModel 
class DataCollectionViewModel @Inject constructor(
    private val dataCollectionSettingsService: DataCollectionSettingsService
) : ViewModel() {
    
    // Enable all services (show in consent dialog)
    fun onConsentToAll() {
        dataCollectionSettingsService.consentToAllServices()
    }
    
    // Enable/disable specific service (show in settings)
    fun onPerformanceToggle(enabled: Boolean) {
        dataCollectionSettingsService.changeDataServiceActivationStatus(
            DataCollectionService.FIREBASE_PERFORMANCE,
            enabled
        )
    }
    
    // Check if user has made initial consent decision
    val hasUserSetConsent: Boolean 
        get() = dataCollectionSettingsService.isDataCollectionAgreementSet
}
```

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

### Testing User Consent System

#### Unit Tests for Consent Management

```kotlin
@Test
fun `when user consents to performance monitoring, Firebase Performance is enabled`() {
    // Given
    val dataCollectionService = DataCollectionSettingsServiceImpl(
        analyticsService = mockAnalytics,
        sharedPreferences = mockPreferences,
        firebaseCrashlytics = mockCrashlytics,
        firebasePerformance = mockPerformance
    )
    
    // When
    dataCollectionService.changeDataServiceActivationStatus(
        DataCollectionService.FIREBASE_PERFORMANCE,
        enabled = true
    )
    
    // Then
    verify(mockPerformance).isPerformanceCollectionEnabled = true
}

@Test
fun `when user revokes performance consent, Firebase Performance is disabled`() {
    // Test revoking consent disables performance monitoring
}
```

#### Testing Performance Monitoring with Consent

```kotlin
@Test
fun `performance monitoring works regardless of consent status`() {
    // Performance monitoring should work gracefully whether consent is given or not
    // When consent is disabled, traces are created but no data is sent to Firebase
}
```

### Testing without User Consent

For development and testing, you can work with performance monitoring even without giving consent:

```kotlin
// Development/Test configuration
@Provides
@Singleton
fun providePerformanceMonitoring(): PerformanceMonitoring {
    return if (BuildConfig.DEBUG) {
        // In debug builds, you can bypass consent for development
        MockPerformanceMonitoring()
    } else {
        // Production respects user consent
        PerformanceMonitoring()
    }
}
```

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

#### GitHub Actions / CI Build Issues (iOS)
**Issue**: Build fails with "Cannot find type 'Trace' in scope" or similar Firebase Performance type errors.

**Root Cause**: CI environments may have different package resolution behavior or module import paths.

**Solutions**:
1. **Explicit Import**: Ensure all Firebase Performance imports are explicit:
   ```swift
   import Foundation
   import os
   import Firebase
   import FirebasePerformance  // Explicit import required for CI
   ```

2. **Package Resolution**: In GitHub Actions, add package resolution step:
   ```yaml
   - name: Resolve Swift Package Dependencies
     run: |
       cd iosApp
       xcodebuild -resolvePackageDependencies -project iosApp.xcodeproj
   ```

3. **Clean Build**: Ensure clean build in CI:
   ```yaml
   - name: Build iOS
     run: |
       cd iosApp
       xcodebuild clean build -project iosApp.xcodeproj -scheme iosApp -configuration Debug
   ```

4. **Firebase Version Pinning**: Consider pinning Firebase iOS SDK version in Package.swift or project settings to ensure consistent builds across environments.

## Production Deployment

### User Consent and Privacy Compliance

#### Pre-Production Consent Checklist

- [ ] **Consent Dialog Implementation**: Verify initial consent dialog appears on first app launch
- [ ] **Settings Integration**: Confirm users can modify consent preferences in app settings
- [ ] **Consent Persistence**: Verify consent choices are saved and persist across app launches
- [ ] **Real-time Updates**: Confirm consent changes take effect immediately without app restart
- [ ] **Default State**: Verify all data collection is disabled by default until user consent
- [ ] **Clear Communication**: Ensure consent dialog clearly explains what data is collected and why

#### Privacy Regulation Compliance

**GDPR Compliance Features:**
- ✅ **Explicit Consent**: Users must actively consent, not just dismiss a dialog
- ✅ **Granular Control**: Users can consent to individual services (Analytics, Crashlytics, Performance)
- ✅ **Easy Withdrawal**: Users can withdraw consent at any time through settings
- ✅ **Clear Information**: Consent dialog explains what data is collected and how it's used
- ✅ **No Default Consent**: All data collection disabled until explicit user consent

**Consent Flow Best Practices:**
1. **First Launch**: Show consent dialog before any data collection
2. **Clear Language**: Use plain language to explain data collection
3. **Easy Access**: Provide clear path to consent settings from main app settings
4. **Respect Choices**: Honor user decisions immediately and persistently

### Pre-Production Checklist

- [ ] **Performance Data Collection**: Verify data appears in Firebase Performance Console (only with consent)
- [ ] **Platform Testing**: Test on physical devices for both Android and iOS
- [ ] **Metrics Validation**: Confirm custom traces and metrics are recorded correctly (with consent)
- [ ] **Load Testing**: Validate performance under various app usage scenarios
- [ ] **Error Handling**: Verify app continues to function when Firebase Performance fails
- [ ] **Consent System**: Test complete consent flow from initial dialog to settings changes

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
