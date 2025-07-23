# iOS Build Issues Analysis

## Current Status
The iOS app build is failing due to SwiftGen configuration issues. The build process gets through dependency resolution and most compilation steps but fails during the SwiftGen script execution phase.

## Root Cause Analysis

### Primary Issue: SwiftGen Path Resolution
- **Problem**: SwiftGen cannot locate the required resource files (`Localizable.strings` and `Assets.xcassets`)
- **Location**: SwiftGen script runs from `/Users/robin/Development/Android/DevFestNantes/iosApp` directory
- **Resources Location**: Files exist at `./iosApp/Resources/en.lproj/Localizable.strings` and `./iosApp/Resources/Assets.xcassets`
- **Config Location**: SwiftGen config is at `iosApp/SwiftGen/swiftgen.yml`

### Configuration Issues Identified
1. **Input Directory Mismatch**: SwiftGen config runs from project root but looks for resources relative to wrong base path
2. **Output Directory**: Current output path causes SwiftGen to look for non-existent directories
3. **Path Resolution**: The relative paths in the config don't match the actual file structure

## Files Structure (Confirmed)
```
iosApp/                              # Xcode project root
├── iosApp/                          # App target directory  
│   ├── Resources/
│   │   ├── Assets.xcassets         ✓ EXISTS
│   │   ├── en.lproj/
│   │   │   └── Localizable.strings ✓ EXISTS
│   │   └── fr.lproj/
│   │       └── Localizable.strings ✓ EXISTS
│   └── SwiftGen/
│       └── swiftgen.yml            ✓ EXISTS
└── iosApp.xcodeproj/
```

## Current SwiftGen Configuration Issues
- `input_dir: ../` - Points to parent of config file location
- `output_dir: SwiftGen/` - Causes path resolution errors
- Input paths: `Resources/en.lproj/Localizable.strings` - Relative to input_dir
- Output paths: Point to generated files in SwiftGen directory

## Build Process Analysis
1. ✅ Package resolution (SPM dependencies) - Working
2. ✅ Code compilation (Swift/Objective-C) - Working  
3. ✅ Linking phase - Working
4. ❌ SwiftGen script execution - **FAILING**
5. ❌ Final app bundle creation - Blocked by SwiftGen failure

## Error Messages
```
swiftgen: error: File SwiftGen not found.
swiftgen: error: It seems like there was an error running SwiftGen.
```

## Fix Plan

### Step 1: Correct SwiftGen Configuration
- Fix `input_dir` and `output_dir` paths to work from the execution context
- Update input file paths to be relative to the correct base directory
- Update output file paths to generate files in the correct location

### Step 2: Path Resolution Strategy
- SwiftGen runs from: `/Users/robin/Development/Android/DevFestNantes/iosApp`
- Config file at: `iosApp/SwiftGen/swiftgen.yml`
- Resources at: `iosApp/Resources/`
- Generated files should go to: `iosApp/SwiftGen/`

### Step 3: Test and Validate
- Test SwiftGen manually before full build
- Verify generated files are created in correct locations
- Run full iOS build to confirm resolution

### Step 4: Build Validation
- Ensure generated Swift files are properly included in Xcode project
- Verify no additional path or dependency issues
- Complete successful iOS app build

## Latest Update: Kotlin/Native Compilation Error (Fixed)

### New Issue Discovered
After fixing the SwiftGen configuration, the build progressed further but failed during Kotlin/Native compilation with the error:
```
No such extension point 'org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetProcessor'
```

### Root Cause
- **Problem**: Version incompatibility between Kotlin (2.2.0) and KSP (2.1.21-2.0.1)
- **Solution**: Updated KSP version from `2.1.21-2.0.1` to `2.2.0-1.0.25` in `gradle/libs.versions.toml`

### Fix Applied
```toml
# Before
ksp = "2.1.21-2.0.1"

# After  
ksp = "2.2.0-1.0.25"
```

## Current Status: Ready for Build Test
1. ✅ SwiftGen configuration - **FIXED**
2. ✅ SwiftGen path resolution - **FIXED** 
3. ✅ Xcode build phase script - **FIXED**
4. ✅ Kotlin/KSP version compatibility - **FIXED**
5. ⏳ Full iOS build test - **PENDING**

## Expected Outcome
After fixing both the SwiftGen configuration and Kotlin/KSP version compatibility, the iOS build should complete successfully, generating:
- `Strings.swift` with localization constants
- `Assets.swift` with asset catalog constants
- Final `DevFest Nantes.app` bundle ready for deployment/testing
