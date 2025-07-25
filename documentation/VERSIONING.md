# Versioning Strategy

This document describes the versioning strategy used for the DevFest Nantes mobile application.

## Calendar Versioning (CalVer)

As of July 2025, the DevFest Nantes mobile app uses **Calendar Versioning (CalVer)** instead of semantic versioning. This change better aligns with our conference-based release schedule.

### Format: `YYYY.0M.MICRO`

- **YYYY**: Full year (e.g., 2025)
- **0M**: Zero-padded month (e.g., 07 for July)
- **MICRO**: Patch version starting from 00

### Examples

- `2025.07.00` - Initial July 2025 release
- `2025.07.01` - First patch release in July 2025
- `2025.10.00` - October 2025 release (DevFest conference time)
- `2026.07.00` - July 2026 release

## Version Management

### Maintainer Responsibility

**Version numbers are managed exclusively by project maintainers:**
- Contributors should **NOT** modify version names or version codes
- Version bumps are handled during the release process
- Current versions are tracked through [GitHub releases](https://github.com/GDG-Nantes/DevfestNantesMobile/releases)

### Platform Differences

Android and iOS app versions may differ due to:
- Platform-specific features
- Different approval processes (App Store vs Play Store)
- Platform-specific patches and updates
- Release timing variations

## Implementation

### Android (`androidApp/build.gradle.kts`)

```kotlin
android {
    defaultConfig {
        versionCode = 28          // Incremental integer
        versionName = "2025.07.00" // CalVer format
    }
}
```

### iOS (`iosApp.xcodeproj/project.pbxproj`)

```
MARKETING_VERSION = 2025.07.00
```

## Migration History

### From Semantic Versioning

The project migrated from semantic versioning to CalVer in July 2025. Specific version numbers and migration details are tracked in the [GitHub releases](https://github.com/GDG-Nantes/DevfestNantesMobile/releases) and [CHANGELOG.md](../CHANGELOG.md).

## Benefits of CalVer

1. **Timeline Clarity**: Version numbers immediately indicate when the release was made
2. **Conference Alignment**: Releases align with DevFest Nantes conference schedule
3. **Predictable Releases**: Clear understanding of release cadence
4. **Industry Standard**: Widely adopted by time-sensitive and event-based projects

## Release Schedule

The DevFest Nantes app typically follows this release pattern:

- **July releases**: Major feature updates and preparation for the conference
- **October releases**: Conference-specific updates and optimizations
- **Patch releases**: Bug fixes and minor improvements as needed

## Version Code Strategy

### Android Version Codes

Version codes continue to increment sequentially regardless of CalVer changes:
- Ensures proper upgrade path on Google Play Store
- Monotonically increasing integers
- Independent of CalVer formatting

### iOS Versioning

iOS uses the CalVer string directly as the `MARKETING_VERSION` without additional version codes.

## When to Increment

### Major Release (Change Month)
- New features
- UI/UX improvements
- Conference data updates
- Significant architectural changes

### Patch Release (Increment MICRO)
- Bug fixes
- Performance improvements
- Minor UI tweaks
- Configuration updates

## Examples in Practice

### Pre-Conference Development (July)
```
2025.07.00 - Initial release with new features
2025.07.01 - Bug fixes and performance improvements
2025.07.02 - Additional bug fixes
```

### Conference Period (October)
```
2025.10.00 - DevFest-specific features and data
2025.10.01 - Live conference updates
2025.10.02 - Hot fixes during the event
```

### Post-Conference (November-June)
```
2025.11.00 - Post-conference improvements
2026.01.00 - New year updates
2026.07.00 - Next major release cycle
```

**Note**: Actual version numbers may vary between Android and iOS platforms based on release timing and platform-specific requirements. Check [GitHub releases](https://github.com/GDG-Nantes/DevfestNantesMobile/releases) for current versions.

## Guidelines for Contributors

### Version Management Rules

1. **DO NOT modify version numbers**: Contributors should never change `versionName`, `versionCode`, or `MARKETING_VERSION` in their pull requests
2. **Focus on features**: Concentrate on implementing features and fixing bugs, not version management
3. **Reference issues properly**: Use issue numbers and clear commit messages instead of version references
4. **Let maintainers handle releases**: Version bumps are part of the release process managed by project maintainers

### Reporting Issues

When reporting bugs or issues:
- Check the current app version from the app's About screen
- Reference the [GitHub releases](https://github.com/GDG-Nantes/DevfestNantesMobile/releases) page for official versions
- Mention platform (Android/iOS) as versions may differ

## Tools and Automation

### Gradle Version Catalog

All dependencies are managed through `gradle/libs.versions.toml` to maintain consistency. Version numbers for the application itself are managed separately by maintainers.

### CI/CD Integration

The CalVer format integrates seamlessly with GitHub Actions and release automation:
- Automatic changelog generation
- Release tagging using CalVer
- App store deployment with proper versioning
- All managed by project maintainers during release cycles

## Migration Guidelines

### For Developers

1. **Never modify versions**: Do not change version numbers in pull requests
2. **Reference GitHub releases**: Use official release pages for version information  
3. **Focus on functionality**: Concentrate on features, not version management
4. **Clear commit messages**: Use descriptive commit messages instead of version references

### For Users

- Version numbers now indicate release timing
- Check the app's About screen or [GitHub releases](https://github.com/GDG-Nantes/DevfestNantesMobile/releases) for current versions
- Android and iOS versions may differ based on platform requirements
- Higher version numbers don't necessarily mean major changes - check changelog for details

## Related Documentation

- [README.md](../README.md) - Project overview with current version
- [CHANGELOG.md](../CHANGELOG.md) - Version history and changes
- [CONTRIBUTING.md](../CONTRIBUTING.md) - Development guidelines and practices

---

**Note**: This versioning strategy may evolve based on project needs and community feedback. Any changes will be documented and communicated clearly to all stakeholders.
