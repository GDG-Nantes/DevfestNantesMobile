# Phase 6 deferred items

Production behavior the Phase 6 tests expose. Each entry is pinned by a test and is not fixed in this phase (parity rule).

## From 06-02 (start-date parsing)

1. **`AgendaViewModel.getDate(date:)` (body now in `AgendaViewModel.parseDate(_:)`) rejects start dates with fractional seconds.** It uses a default-option `ISO8601DateFormatter`, which returns nil for `2025-10-16T09:00:00.000+02:00`, so such a start date falls back to `Date()` and the session is placed at the current time. The core/model fixture start dates carry no fractional seconds, so the gap is latent as far as known. Pinned by `AgendaDateParsingTests.falls_back_to_now_for_fractional_seconds`; not fixed (parity). The Phase 9 shared grouping must reproduce this or change it as a recorded deviation.

## From 06-06 (developer environment, Xcode GUI build)

1. **Xcode launched from the Dock cannot run the `:shared:embedAndSignAppleFrameworkForXcode` Run Script when the default macOS JDK is x86_64-only.** Xcode started from the Dock does not inherit the shell `JAVA_HOME`, so `./gradlew` falls back to `/usr/libexec/java_home`, whose default on this machine is an x86_64-only Corretto 11. Rosetta is not installed, so the build fails with "Bad CPU type in executable". Not a code regression (the same tree builds from the CLI). Workaround used during the 06-06 smoke: `launchctl setenv JAVA_HOME "/Applications/Android Studio.app/Contents/jbr/Contents/Home"` (not persistent across reboot). Durable fix, out of scope for Phase 6: register an ARM64 JDK as the system default, or make the Run Script resolve `JAVA_HOME` itself. Developer-environment follow-up.
