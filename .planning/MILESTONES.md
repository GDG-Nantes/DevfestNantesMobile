# Milestones

## v1.0 Modernization (Shipped: 2026-10-16)

**Delivered:** A working, cached CI on Android and iOS, a modern build toolchain, a multi-module `core-*`/`feature-*` architecture, Koin DI instead of Hilt, and real test coverage. Users see no change in behavior.

**Phases completed:** 1–5 (5 phases, 37 plans, 88 tasks)

**Key accomplishments:**
- iOS CI fixed at the root cause: Xcode and the simulator are now resolved at run time instead of pinned. Gradle caching is branch-aware (read-only on PRs) and there is a Konan cache, and the Android and iOS jobs run separately (PR #417).
- Toolchain upgraded: Kotlin 2.4.20, AGP 9.4.0 with `com.android.kotlin.multiplatform.library`, Gradle 9.7.1, Compose BOM 2026.09.00, Apollo 5.2.0, Firebase BOM 34.19.0 and kotlinx-datetime 0.8.0. The settings file moved to `settings.gradle.dcl` (PR #418).
- The codebase is split into `build-logic` convention plugins, six `core-*` modules and six `feature-*` modules, with `:shared` reduced to a source-less iOS umbrella framework (PR #419).
- Dagger Hilt is replaced by Koin 4.2.2: one Koin module per Gradle module, composed by `initKoin()` on Android and iOS. Koin `verify()` graph tests and a CI-enforced DI gate check the wiring (PR #420).
- Test coverage retrofitted: random stubs replaced by fixtures, GraphQLStore and the mappers tested offline against Apollo MockServer, all ten ViewModels unit-tested with fakes, and a Compose smoke test in each of the six feature modules (PR #421).
- The OpenFeedback crash (`NoClassDefFoundError kotlinx/datetime/Instant`) is fixed by the kotlinx-datetime compat pin, and a JVM linkage test now guards it.

**Stats:**
- 328 files changed, +9,795 / −2,005 lines (excluding `.planning/`)
- About 15.8k lines of Kotlin and 3.8k lines of Swift
- Timeline: 2026-09-17 → 2026-10-16 (about 30 days); requirements defined 2026-09-12
- Git range: `aba435a` (Phase 1, #417) → `b89dd5b` (Phase 5, #421)

**Audit:** `milestones/v1.0-MILESTONE-AUDIT.md`, status tech_debt. Requirements 25/25, integration 6/6, flows 7/7.

**Closeout:** override_closeout. Known verification overrides: 2 newly acknowledged, 0 carried forward from a prior close (see STATE.md Deferred Items).

### Known Tech Debt (accepted)
- GraphQLStore uses the deprecated `speakers` field, so only 100 of 110 speakers are returned. It also logs with `println` instead of Timber.
- `RoomSortIndex` does not know the room ids `Tour Bretagne` and `Unconf`, so both rooms sort last.
- `SocialItem.Builder` produces double-slash links for bare handles, and a social with an unknown name and no URL becomes an empty website link.
- The Speaker screen's page-view analytics event never fires (pre-existing, 02-REVIEW WR-01).
- CI: `cancel-in-progress: true` applies to pushes to main (accepted in 01-UAT). The 01-REVIEW WR-02 and WR-03 hardening was not done.
- Nyquist: phases 2 and 3 were never validated, and phase 4 is only partly compliant.

---
