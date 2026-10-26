# Phase 6: Socle de tests et gates CI iOS - Research

**Researched:** 2026-10-26
**Domain:** iOS test lane (xcodebuild + `.xctestplan` + Swift Testing, hosted bundle), GitHub Actions macOS CI, bash interop gates, KMP-NativeCoroutines version alignment
**Confidence:** HIGH (every critical mechanism below was reproduced in a scratch copy of the repo on Xcode 27.1 / iOS 27 simulator, not only read in docs)

## Summary

The phase is "standard pattern" and it is: every recommended mechanism was **prototyped end to end** in a scratch copy of the repo (outside the working tree). Results: a hosted Swift Testing bundle on the existing `TEST_HOST`/`BUNDLE_LOADER` setup compiles and runs; a committed `.xctestplan` wired into the shared scheme works with `build-for-testing` then `test-without-building`; `xcresulttool get test-results summary` exposes `totalTestCount`; the archived `swift-names-gate.sh` restores with only path edits and passes `check` and `selftest`; the KMP-NativeCoroutines SPM bump to `1.0.6` compiles and the tests stay green.

Four findings change or sharpen CONTEXT.md decisions and the planner must act on them:

1. **`iOSApp.init` neutralisation is not enough (D-02).** `ContentView` creates `BaseViewModel()` (`@StateObject`) and `AgendaView`/`RCValues` start Koin, network and Remote Config when the host app renders its `WindowGroup`. The `body` must also render an empty view under test. Verified: `init` early-return + `Color.clear` body = host launches cleanly.
2. **`AgendaViewModel.getDate(date:)` is not testable as written (D-01).** `AgendaViewModel.init()` calls `super.init()` which evaluates `IosDependencies.shared.store()`, which self-starts Koin. Either extract `getDate` into a pure static helper (parity-neutral refactor, same commit) or limit the test set to `SessionFilter` (CONTEXT says "si testable", so dropping it is allowed).
3. **`Package.resolved` is not tracked** (`*.xcworkspace` is gitignored), so "Package.resolved à mettre à jour" in CONTEXT is a no-op for git. CI resolves fresh each run; only the `project.pbxproj` pin matters for the KMP-NC bump and the gate.
4. **The DerivedData cache step currently runs before Xcode is selected** in `ios.yml`, and its `restore-keys` have no Xcode component. Satisfying D-07 means moving the step after Xcode selection and putting the Xcode version in the key **and in every restore-key prefix**, otherwise a cache from another Xcode still restores.

Also verified: `xcodebuild test-without-building` exits **0** and prints `** TEST EXECUTE SUCCEEDED **` when zero tests ran (`totalTestCount` 0, `result` "unknown"), which is exactly why the explicit xcresult assertion (D-05) is required.

**Primary recommendation:** Rewrite the existing target in place or recreate it as `DevFestNantesTests` (hosted, iOS 17.0, Swift 5), add `iosApp/iosApp.xctestplan`, switch the scheme `TestAction` to `<TestPlans>`, guard `iOSApp` init **and** body on `XCTestConfigurationFilePath`, then in `ios.yml` replace the `build` step by `build-for-testing` / `test-without-building` + a count-asserting script, restore the two gates (`swift-names-gate.sh`, new `kmp-nc-version-gate.sh`) and finally bump the pbxproj pin to `1.0.6` in the same commit that introduces the version gate.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

### Premier test réel
- **D-01:** La nouvelle target contient quelques tests Swift Testing de **logique Swift pure**,
  sans réseau ni Koin : au minimum l'encodage/décodage `Codable` de `SessionFilter`
  (`iosApp/iosApp/Model/SessionFilter.swift`, format des clés `UserDefaults` legacy que la
  Phase 9 doit relire) et `AgendaViewModel.getDate(date:)` si testable sans déclencher
  `BaseViewModel`/`IosDependencies`. Pas de test canari trivial, pas de caractérisation de
  l'agenda (construction de `Session` Kotlin depuis Swift trop lourde ici).
- **D-02:** Target **hébergée par l'app** (`TEST_HOST`/`BUNDLE_LOADER` conservés, pour
  `@testable import` du code app tant qu'il n'est pas en packages). `iOSApp.init` est
  **neutralisé sous XCTest** : `FirebaseApp.configure()`, `IosDependencies.shared.start()`,
  traces `PerformanceMonitoring` et `RCValues.sharedInstance` ne s'exécutent pas quand le
  process tourne sous test. Le comportement en production reste strictement identique (parité).
  — **Reversibility:** reversible — garde locale à `iOSApp.swift`, retirée quand la Phase 10/11
  sort la logique de l'app host.
- **D-03:** **Supprimer** la target `DevFest NantesTests` et ses deux fichiers
  (`DevFest_NantesTests.swift` cassé, `MockDevFestNantesStore.swift` entièrement commenté) ;
  créer une target neuve au nom sans espace (ex. `DevFestNantesTests`), référencée par le
  scheme partagé `iosApp` via un `.xctestplan` commité.

### Structure du job CI
- **D-04:** **Un seul job** dans `ios.yml` : l'étape `xcodebuild build` est remplacée par
  `build-for-testing` puis `test-without-building` (une seule compilation de l'app). On ne
  découpe en plusieurs jobs que si un dépassement du `timeout-minutes: 30` est mesuré.
- **D-05:** Assertion `.xcresult` : le job **échoue si 0 test** exécuté (comptage via
  `xcresulttool`). Pas de seuil minimum à maintenir.
- **D-06:** Robustesse retenue (toutes) : artefact `.xcresult` téléversé en cas d'échec ;
  simulateur résolu **par UDID** (toujours le dernier iPhone dispo, sélection dynamique v1.0
  conservée) puis `xcrun simctl boot` + `bootstatus -b` ; `-parallel-testing-enabled NO` et
  aucun retry ; version Xcode, simulateur et nombre de tests écrits dans `$GITHUB_STEP_SUMMARY`.
- **D-07:** La clé de cache DerivedData inclut la version de Xcode (`xcodebuild -version`),
  exigé par le critère de succès 2.

### Restauration des gates
- **D-08:** `swift-names-gate.sh` restauré dans `scripts/` ; ses fichiers de baseline dans
  `scripts/swift-names/`, **sans le préfixe `03-`** (exclusions, noms référencés, membres,
  en-tête). Baseline **regénérée sur l'en-tête `shared.h` actuel** avant branchement. Les
  chemins internes du script sont mis à jour en conséquence.
- **D-09:** La CI lance `check` **et** `selftest` (le selftest réutilise l'en-tête déjà lié ;
  il prouve que le gate détecte toujours un échange d'identité et une collision).
- **D-10:** Gate KMP-NC : **script dédié** `scripts/kmp-nc-version-gate.sh`, qui compare
  `kmpNativeCoroutines` dans `gradle/libs.versions.toml` au pin du `project.pbxproj`
  (doit être `kind = exactVersion`), échoue s'ils divergent ou si le pin n'est pas exact.
  Conçu pour accepter plus tard un `Package.swift` (Phase 11). Tourne tôt dans le job (avant
  Gradle) et en local.

### Validation du bump KMP-NC
- **D-11:** Preuve de parité après `1.0.0-ALPHA-45` → `1.0.6` : **smoke manuel guidé**
  (checkpoint humain) sur simulateur : Agenda (sessions, filtres, ajout/retrait favori,
  relance de l'app), détail de session, Speakers, Venue, About ; captures à l'appui.
- **D-12:** Ordre : nouvelle target + lane CI d'abord, puis gates, puis bump KMP-NC sous
  le filet. Commits séparés et réversibles ; le gate KMP-NC est introduit **dans le même
  commit** que le bump (sinon il échouerait sur `main`).

### Claude's Discretion
- Mécanisme exact de détection « sous XCTest » dans `iOSApp.init` (env
  `XCTestConfigurationFilePath`, `NSClassFromString("XCTestCase")`…), tant que la prod est
  inchangée.
- Deployment target et `SWIFT_VERSION` de la nouvelle target (aligner sur l'app ; iOS 17
  partout est en Phase 8, Swift 6 en Phase 12).
- Cache SPM séparé (`-clonedSourcePackagesDirPath`), composition exacte des clés de cache
  au-delà de la version Xcode.
- Nom exact du `.xctestplan` et de la target, emplacement du parsing `xcresulttool`
  (inline ou script).
- Mise à jour de `README.md`, `documentation/DEVELOPMENT.md`, `scripts/README.md` et
  `AGENTS.md` (commandes de test iOS, gates) dans les mêmes commits (règle AGENTS.md).

### Deferred Ideas (OUT OF SCOPE)
- `resources-gate.sh` (v1.0 Phase 3) : preuve de migration de ressources **Android** one-shot,
  inutilisable telle quelle. À reconsidérer sous forme de gate iOS (`en.lproj`/`fr.lproj`
  `Localizable.strings` aux clés identiques) en Phase 11 avec les ressources SwiftGen en
  packages, ou en invariant Android permanent dans une phase dédiée.
- Seuil minimum de tests configurable (au-delà de « 0 test = échec ») : à revoir quand les
  Phases 8/12 auront ajouté des tests.
- Découpage du job iOS en plusieurs jobs : seulement si le timeout de 30 min est dépassé
  (naturellement reconsidéré en Phase 11 avec l'XCFramework).
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| IOSTEST-03 | La CI iOS exécute `xcodebuild test` via un test plan, échoue si aucun test n'est exécuté, clé de cache avec version Xcode (Phase 6 : lane + target Swift Testing; les tests de packages viennent en Phase 11) | Hosted Swift Testing target, `.xctestplan` + scheme `<TestPlans>`, `build-for-testing`/`test-without-building` flow, `xcresulttool` `totalTestCount` assertion script, Xcode-aware cache key, UDID simulator resolution (all prototyped) |
| IOSTEST-04 | `swift-names-gate.sh` restauré dans `scripts/` et exécuté en CI | Archived script restored with 5 path edits; `check` and `selftest` verified green on the current `shared.h`; baseline regeneration diff measured (only `shared-h-baseline.txt` changes) |
| CONC-03 | KMP-NativeCoroutines en 1.0.6 côté SPM comme côté Gradle, gate CI de divergence | pbxproj pin edit, tag/revision facts for `v1.0.6`, release-note diff ALPHA-45 to 1.0.6 (Swift API unchanged), draft `kmp-nc-version-gate.sh` verified on 3 cases, compile + test verified at 1.0.6 |
</phase_requirements>

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Swift unit tests (SessionFilter Codable) | Xcode test bundle (hosted in the app) | — | Pure Swift logic, runs inside the app host on the simulator |
| Test plan / scheme wiring | Xcode project (`iosApp.xctestplan`, `iosApp.xcscheme`) | CI invokes by `-testPlan` | The plan is the committed source of truth for which targets run; later phases only append targets |
| "Under test" app bootstrap guard | App target (`iOSApp.swift`) | — | The host app is launched by XCTest; only the app can avoid Firebase/Koin/Remote Config startup |
| Build-once, test-without-building orchestration | GitHub Actions job (`ios.yml`) | xcodebuild | One compile, then repeatable test run, simulator booted by UDID |
| "0 tests = failure" assertion | bash script over `.xcresult` (`xcresulttool`) | CI step | xcodebuild itself exits 0 on zero tests (verified) |
| Swift-visible-name interop gate | bash script over generated `shared.h` | Gradle (`linkDebugFrameworkIosSimulatorArm64`) | Header is produced by Kotlin/Native; the gate only reads it |
| KMP-NC version-skew gate | bash script over `libs.versions.toml` + `project.pbxproj` | CI (runs before Gradle) | Pure text comparison; must work with no toolchain, locally and early in CI |
| Cache correctness (Xcode-aware) | GitHub Actions cache keys | — | DerivedData is only valid for the Xcode that produced it |

## Standard Stack

### Core
| Library / Tool | Version | Purpose | Why Standard |
|----------------|---------|---------|--------------|
| Swift Testing (toolchain `Testing`) | Testing Library Version 2084 on Xcode 27.1 `[VERIFIED: local run output "Testing Library Version: 2084"]` | New test target | Ships with Xcode 16+; no package needed (CONTEXT: "Swift Testing de la toolchain uniquement") |
| xcodebuild `build-for-testing` / `test-without-building` + `.xctestplan` | Xcode 26.6 on `macos-latest` runner (macOS 26 arm64, default) `[CITED: github.com/actions/runner-images macos-26-arm64-Readme]`; Xcode 27.1 locally | Test lane | Apple-supported split of compile and run |
| `xcrun xcresulttool get test-results summary` | Xcode 27.1 `[VERIFIED: local run]` (new-style API, `get --format json` legacy API is the deprecated one) | Count executed tests | Only reliable signal that tests ran |
| KMP-NativeCoroutines (SPM) | `1.0.6` (tag `v1.0.6` -> revision `54a45fbb15bc0bebc54081f87683830d3bc9e84e`) `[VERIFIED: git ls-remote + gh release list, published 2026-09-07]` | Swift side of Kotlin coroutines bridge | Must equal Gradle plugin version (README rule "Make sure to always use the same versions for all the libraries!") `[CITED: github.com/rickclephas/KMP-NativeCoroutines README]` |
| bash + awk + sed + jq | jq 1.8.2 on the runner `[CITED: runner-images readme]`; jq present locally `[VERIFIED: which jq]` | Gates and assertion scripts | Matches existing `scripts/di-gate.sh` style |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `actions/upload-artifact` | `@v4` for consistency with `actions/checkout@v4` / `cache@v4` already in the workflows; newer majors exist (v7.0.2 is latest) `[VERIFIED: gh release list]` | Upload `.xcresult` on failure | `if: failure()` step. Pick v4 (repo convention) unless the planner decides to bump all actions; Dependabot only watches `gradle` `[VERIFIED: .github/dependabot.yml]` |
| xcbeautify | 3.2.1 preinstalled on runner `[CITED: runner-images readme]` | Pretty logs | Optional. If piped, `set -o pipefail` is mandatory (default `run` shell has no pipefail). Prefer not piping |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| In-place rewrite of the existing target objects | Delete + recreate target with new IDs (D-03 literal) | End state identical. In-place rename minimises pbxproj surgery; delete+create is cleaner semantically. No `xcodeproj` Ruby gem is installed `[VERIFIED: gem list]`, so both are hand/Python edits of `project.pbxproj` (objectVersion 54: no synchronized folders) |
| `XCTestConfigurationFilePath` env check | `NSClassFromString("XCTestCase") != nil`; or a test-plan env var | Both signals verified present (see Code Examples). The env var is the conventional one; the class check also works but links the idea of XCTest into app code |
| Inline `xcresulttool` parsing in the workflow | `scripts/xcresult-assert-tests.sh` | Script is testable locally against a real `.xcresult` (verified) and reusable by Phase 11; recommended |

**Installation:** no new packages. Only the existing SPM pin changes (`1.0.0-ALPHA-45` -> `1.0.6`) in `project.pbxproj`.

**Version verification:**
```bash
git ls-remote https://github.com/rickclephas/KMP-NativeCoroutines.git "refs/tags/v1.0.6"
# 54a45fbb15bc0bebc54081f87683830d3bc9e84e  refs/tags/v1.0.6      [VERIFIED this session]
git ls-remote ... "refs/tags/v1.0.0-ALPHA-45"
# 131f600110c4ba215585cb03ec5d7b6ad1c1fde2  refs/tags/v1.0.0-ALPHA-45  (== currently resolved revision)
```
Tags are `v`-prefixed on GitHub but SwiftPM exposes them as `1.0.6` (verified: `xcodebuild -resolvePackageDependencies` printed "Checking out 1.0.6 of package 'KMP-NativeCoroutines'").

## Package Legitimacy Audit

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| KMP-NativeCoroutines (SPM, already a dependency; version bump only) | SwiftPM (git tag) | years (ALPHA-45 pinned since 2025-06) | n/a | github.com/rickclephas/KMP-NativeCoroutines (tag verified via `git ls-remote`) | n/a (seam covers npm/pypi/crates only) | Approved - existing dependency, same repo, new tag |
| Swift Testing | Xcode toolchain | - | - | Apple/swift-testing (bundled) | n/a | Approved - no install |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none. No new third-party package is introduced in this phase.

## Architecture Patterns

### System Architecture Diagram

```
PR / push to main
   |
   v
[ios.yml, single macOS job, timeout 30 min]
   |
   |-- checkout
   |-- (1) scripts/kmp-nc-version-gate.sh ------ reads --> gradle/libs.versions.toml (kmpNativeCoroutines)
   |        (no toolchain needed, runs first)              iosApp/iosApp.xcodeproj/project.pbxproj (pin)
   |        fail: SKEW / NOT-EXACT
   |-- android-setup (JDK/Gradle), Konan + Homebrew + SwiftGen caches
   |-- Select latest Xcode_*.app  --> output xcode_version = "<ver>-<build>"
   |-- Cache DerivedData  (key includes xcode_version; restore-keys too)
   |-- Resolve simulator (jq) --> UDID + name  --> simctl boot + bootstatus -b
   |-- Build shared framework (Gradle embedAndSign) --> shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework/Headers/shared.h
   |-- (2) scripts/swift-names-gate.sh check ; selftest ---- reads shared.h + scripts/swift-names/*.txt + git-tracked iosApp/**/*.swift
   |-- KMP iOS tests (Gradle iosSimulatorArm64Test)
   |-- SwiftGen, mock GoogleService-Info.plist, -resolvePackageDependencies
   |-- xcodebuild build-for-testing  -scheme iosApp -testPlan iosApp  (one app compile)
   |-- xcodebuild test-without-building  -resultBundlePath $RUNNER_TEMP/ios-tests.xcresult  -parallel-testing-enabled NO
   |        |
   |        v  simulator boots host app "DevFest Nantes" -> iOSApp guard (XCTestConfigurationFilePath set)
   |        |       -> no Firebase / Koin / RC / perf, body = Color.clear -> DevFestNantesTests.xctest (Swift Testing) runs
   |-- (3) scripts/xcresult-assert-tests.sh <xcresult>  --> totalTestCount>=1 && passed>=1, else fail
   |-- write Xcode / simulator / test count to $GITHUB_STEP_SUMMARY
   '-- if: failure()  upload .xcresult artifact
```

### Recommended Project Structure
```
iosApp/
├── iosApp.xctestplan                 # NEW - default plan of the shared scheme (container: = iosApp/ dir)
├── DevFestNantesTests/               # NEW dir (no space); replaces "DevFest NantesTests/"
│   └── SessionFilterTests.swift      #   Swift Testing, @testable import DevFest_Nantes
├── iosApp/                           # app target "DevFest Nantes" (module DevFest_Nantes); iOSApp.swift guard
└── iosApp.xcodeproj/
    └── xcshareddata/xcschemes/iosApp.xcscheme   # TestAction: <TestPlans> instead of <Testables>
scripts/
├── swift-names-gate.sh               # restored (paths edited)
├── swift-names/                      # exclusions, referenced names, members baseline, shared.h baseline (no "03-" prefix)
├── kmp-nc-version-gate.sh            # NEW
├── xcresult-assert-tests.sh          # NEW (recommended)
└── di-gate.sh (existing, model for style)
```

### Pattern 1: Hosted Swift Testing target (verified)
**What:** Keep `TEST_HOST` / `BUNDLE_LOADER` so `@testable import DevFest_Nantes` works.
**When to use:** Until Phase 10/11 move logic out of the app target.
**Verified facts (scratch run, Xcode 27.1):**
- Module name of the app is `DevFest_Nantes` (target/product "DevFest Nantes").
- Swift Testing tests are discovered and run in the hosted bundle with `SWIFT_VERSION = 5.0` and `IPHONEOS_DEPLOYMENT_TARGET = 17.0` for the test target; no extra build setting is needed.
- `shared.framework` is found by the test target without adding any `FRAMEWORK_SEARCH_PATHS` (the app's existing mechanism suffices). `[VERIFIED: scratch build-for-testing succeeded]`
- `CODE_SIGNING_ALLOWED=NO` works for the simulator.

Existing test target settings, from `iosApp/iosApp.xcodeproj/project.pbxproj:830-851` (to be replaced, not copied):
```
BUNDLE_LOADER = "$(TEST_HOST)";
HEADER_SEARCH_PATHS = "\"${SRCROOT}/Pods/Firebase/CoreOnly/Sources\"";   // stale CocoaPods path - drop
IPHONEOS_DEPLOYMENT_TARGET = 15.0;                                       // set 17.0 (matches app)
PRODUCT_BUNDLE_IDENTIFIER = "com.DevFest-NantesTests";
SWIFT_VERSION = 5.0;
TEST_HOST = "$(BUILT_PRODUCTS_DIR)/DevFest Nantes.app/$(BUNDLE_EXECUTABLE_FOLDER_PATH)/DevFest Nantes";
```
The old target also carries a broken build phase `454FFC6B2083FF400649C68F /* [CP] Check Pods Manifest.lock */` (CocoaPods is not used; it `exit 1`s when `Podfile.lock` is absent) - it must disappear with the old target. `[VERIFIED: project.pbxproj:499-516]`

**pbxproj objects belonging to the old target (all must go or be rewritten; IDs read from the file this session):**
- `PBXBuildFile`: `CB3979D0...` (DevFest_NantesTests.swift in Sources), `CB4041DB...` (MockDevFestNantesStore.swift in Sources)
- `PBXFileReference`: `CB3979CD...` (`DevFest NantesTests.xctest`), `CB3979CF...` (DevFest_NantesTests.swift), `CB4041DA...` (MockDevFestNantesStore.swift)
- `PBXGroup`: `CB3979CE... /* DevFest NantesTests */` (and its entries in the main group and the Products group)
- `PBXNativeTarget`: `CB3979CC...`; phases `CB3979C9` Sources, `CB3979CA` Frameworks, `CB3979CB` Resources; `PBXTargetDependency` `CB3979D2` (+ its container item proxy); `XCBuildConfiguration` `CB3979D3`/`CB3979D4`; `XCConfigurationList` `CB3979D5`
- `PBXProject.attributes.TargetAttributes`: `CB3979CC2919B78E00BDAE6F = { CreatedOnToolsVersion = 14.1; TestTargetID = 7555FF7A242A565900829871; }` and the entry in `targets = (...)`
- Scheme `iosApp.xcscheme`: `<TestableReference>` for `BlueprintIdentifier = "CB3979CC2919B78E00BDAE6F"` / `BlueprintName = "DevFest NantesTests"`
After editing: `plutil -lint iosApp/iosApp.xcodeproj/project.pbxproj` and `xcodebuild -project iosApp/iosApp.xcodeproj -list` must succeed. **New file paths must be added to the target's Sources phase** (objectVersion 54 has no folder auto-inclusion).

### Pattern 2: `.xctestplan` + scheme (verified end to end)
`iosApp/iosApp.xctestplan` (verified working; replace `identifier` with the new target's blueprint ID and `name` with the new target name):
```json
{
  "configurations" : [
    { "id" : "5A4C4A52-3B8F-4E2B-9C6C-0C1D2E3F4A5B", "name" : "Default", "options" : { } }
  ],
  "defaultOptions" : { "codeCoverage" : false },
  "testTargets" : [
    {
      "target" : {
        "containerPath" : "container:iosApp.xcodeproj",
        "identifier" : "<BLUEPRINT_ID_OF_DevFestNantesTests>",
        "name" : "DevFestNantesTests"
      }
    }
  ],
  "version" : 1
}
```
Scheme `TestAction`: remove `<Testables>…</Testables>`, add
```xml
<TestPlans>
   <TestPlanReference
      reference = "container:iosApp.xctestplan"
      default = "YES">
   </TestPlanReference>
</TestPlans>
```
`container:` resolves relative to the directory holding `iosApp.xcodeproj`, so the plan lives at `iosApp/iosApp.xctestplan`. `[VERIFIED: scratch run, both commands below succeeded with this layout]`. The scheme's `BuildActionEntry` for the app may keep `buildForTesting = "NO"`; the plan pulls in the test target (verified).

Commands (verified; `$UDID` from the simulator step):
```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -testPlan iosApp -configuration Debug \
  -destination "platform=iOS Simulator,id=$UDID" -derivedDataPath build \
  build-for-testing CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO CODE_SIGN_IDENTITY="" PROVISIONING_PROFILE=""

xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -testPlan iosApp -configuration Debug \
  -destination "platform=iOS Simulator,id=$UDID" -derivedDataPath build \
  -resultBundlePath "$RUNNER_TEMP/ios-tests.xcresult" -parallel-testing-enabled NO \
  test-without-building
```
`build-for-testing` writes `build/Build/Products/iosApp_iosApp_iphonesimulator*.xctestrun` (observed). `-resultBundlePath` must not pre-exist (`rm -rf` first). Keep the result bundle **outside** the cached `build/` directory.

### Pattern 3: Neutralising the host app under test (verified)
Observed in a hosted Swift Testing run: `XCTestConfigurationFilePath`, `XCTestBundlePath`, `XCTestBundleInjectPath`, `XCTestSessionIdentifier` are all set in the host process, and `NSClassFromString("XCTestCase")` resolves. `[VERIFIED: scratch run output "PROBE-ENV keys=[\"XCTestBundleInjectPath\", \"XCTestBundlePath\", \"XCTestConfigurationFilePath\", \"XCTestSessionIdentifier\"]"]`. With only the `init` guard the body would still build `ContentView`, which holds `@StateObject var viewModel = BaseViewModel()` (`iosApp/iosApp/UI/ContentView.swift:7`), and `BaseViewModel.init(store: DevFestNantesStore = IosDependencies.shared.store())` (`Model/BaseViewModel.swift:17`) -> `IosDependencies.store()` is documented as "Self-starting, so a ViewModel created before the app entry point has called [start] (SwiftUI previews, test hosts) does not crash" (`shared/.../IosDependencies.kt`) - i.e. it silently starts Koin. `AgendaView` additionally calls `RCValues.sharedInstance.fetchCloudValues()` (`Agenda/AgendaView.swift:57`) which needs a configured Firebase. Hence the body guard.

### Anti-Patterns to Avoid
- **Guarding only `init`:** the SwiftUI scene still renders `ContentView` and starts Koin / network / Remote Config.
- **Pinning an Xcode version in the workflow:** v1.0 decision is dynamic selection; the answer is a cache key that follows the Xcode version, not a pin.
- **`restore-keys` without the Xcode component:** `${{ runner.os }}-xcode-deriveddata-` alone would restore another Xcode's DerivedData.
- **Trusting `** TEST EXECUTE SUCCEEDED **`:** verified to print with zero tests.
- **Selecting the simulator by name only:** names repeat across runtimes; use the UDID.
- **Piping xcodebuild without `pipefail`.**

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Counting executed tests | Parsing xcodebuild stdout (`Executed N tests` is XCTest-only: Swift Testing runs print `Executed 0 tests` for the XCTest half while 2 Swift Testing tests ran, observed) | `xcresulttool get test-results summary --compact` -> `.totalTestCount`, `.passedTests` | Covers both XCTest and Swift Testing; stable JSON |
| Picking the simulator | `grep`/`sed` over `simctl list` text | `simctl list devices available --json` + `jq` | Snippet below is verified |
| Test-target selection | Custom `-only-testing` lists in CI | The committed `.xctestplan` | Phase 11 only appends package targets to the plan |
| Test framework | Custom assertion helpers / mocks | Swift Testing `#expect`/`#require` | Project rule: no mocking library |
| Header-name diffing | New gate | Restore `swift-names-gate.sh` | Already encodes the Phase 3 lessons (identity swap, collision count) |
| pbxproj editing | Manual text patching by eye | A small Python (or Ruby `xcodeproj`) script + `plutil -lint` + `xcodebuild -list` | Object IDs and cross references are easy to break |

**Key insight:** every failure mode this phase guards against (silent zero-test green, stale cache after Xcode drift, Swift name identity swap, SPM/Gradle skew) is *silent* by default. Each guard must be validated by a failing case, not only a passing one (the gates ship `selftest`; the xcresult script was verified against a real zero-test bundle).

## Runtime State Inventory

Not a rename/refactor phase in the data sense; checked for stale runtime state around the test-target replacement:

| Category | Items Found | Action Required |
|----------|-------------|------------------|
| Stored data | None - the phase does not touch persisted keys (`Favorites`, `AgendaSessionFilters` are only read in tests via fixed payloads) | none |
| Live service config | None | none |
| OS-registered state | None | none |
| Secrets/env vars | `iosApp/iosApp/GoogleService-Info.plist` is gitignored `[VERIFIED: git check-ignore]`; CI creates a mock. No secret names change | none |
| Build artifacts | `iosApp/DevFestNantes.xcarchive/` is a stray local archive in the tree (untracked, unrelated). CI cache `build` + `~/Library/Developer/Xcode/DerivedData` hold the old target's products until the key changes (the new key invalidates them) | none beyond the key change |

## Common Pitfalls

### Pitfall 1: Host app starts Koin / Firebase / Remote Config under test
**What goes wrong:** Koin start and network calls from the host app (verified by reading the code path), and very likely a crash from Remote Config used without `FirebaseApp.configure()` `[ASSUMED: not reproduced]`, hence flaky tests.
**Why:** `WindowGroup { ContentView() }` renders when the host launches; `BaseViewModel` self-starts Koin.
**How to avoid:** guard `init` and `body` (Code Examples). Verified working.
**Warning signs:** simulator log shows `[FirebaseRemoteConfig]` / Apollo traffic during tests; host-app crash before the first test.

### Pitfall 2: Zero tests executed but the job is green
**What goes wrong:** target dropped from the plan, test discovery broken -> `** TEST EXECUTE SUCCEEDED **`, exit 0.
**Verified evidence:** `-only-testing:LibTests/Nope` run: rc 0, `{"passedTests":0,"failedTests":0,"totalTestCount":0,"result":"unknown"}`.
**How to avoid:** `scripts/xcresult-assert-tests.sh` requires `totalTestCount >= 1` and `passedTests >= 1` (so an all-skipped run also fails).

### Pitfall 3: Cache restored across Xcode versions
**What goes wrong:** module cache / DerivedData from another Xcode -> bogus compile errors or wrong results.
**Why:** current workflow computes the cache key before the "Select latest installed Xcode" step and `restore-keys` end in a version-less prefix. `[VERIFIED: .github/workflows/ios.yml]`
**How to avoid:** move the DerivedData cache step after Xcode selection; key `…-xcode-${{ steps.xcode.outputs.version }}-…`; every restore-key keeps the `xcode-<version>` segment. Version string: `xcodebuild -version | awk 'NR==1{v=$2} NR==2{b=$3} END{print v "-" b}'` -> `27.1-27A9275` locally `[VERIFIED]`.

### Pitfall 4: `xcodebuild -resolvePackageDependencies -derivedDataPath` is rejected
**Verified message:** "The flag -scheme, -testProductsPath, or -xctestrun is required when specifying -derivedDataPath." Use `-clonedSourcePackagesDirPath <dir>` (discretion item) for the resolve step, and pass the same dir to the build/test steps, or leave the resolve step as today (default location, no `-derivedDataPath`). If a separate SPM cache is wanted, key it on the pbxproj hash (+ Xcode version) and point both at `-clonedSourcePackagesDirPath`.

### Pitfall 5: Simulator not booted / wrong runtime
**How to avoid:** resolve by UDID with the latest runtime then latest `iPhone N`, then `xcrun simctl boot "$UDID" || true` (already-booted returns an error) and `xcrun simctl bootstatus "$UDID" -b`. Existing logic picks only by name; a name can exist in several runtimes.

### Pitfall 6: SessionFilter encode order is non-deterministic
**What goes wrong:** a test asserting the exact JSON string of an encoded `Set<SessionFilter>` flakes (Set iteration order).
**How to avoid:** assert on **decoding** a fixed legacy payload and on round-trip equality as `Set`s; never on byte-equality of encoded sets. The legacy format is a JSON array of `{"type": <FilterType raw>, "value": <String>}`; `FilterType` raw values are the case names `bookmark, complexity, language, room, type`. `[VERIFIED: iosApp/iosApp/Model/SessionFilter.swift]`, quoted: `enum FilterType: String, Codable { case bookmark, complexity, language, room, type }`.

### Pitfall 7: `swift-names-gate.sh` is macOS-only and links Gradle
It uses BSD `sed -i ''` (selftest) -> macOS runner only (fine: `ios.yml` is `macos-latest`). Each mode without `SWIFT_GATE_HEADER` runs `./gradlew --no-daemon -q :shared:linkDebugFrameworkIosSimulatorArm64`; running `check` then `selftest` pays two JVM starts. Either run both after "Build shared framework" (task up to date) or export `SWIFT_GATE_HEADER=shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework/Headers/shared.h` for the step (verified path exists locally with 143.3K header).

### Pitfall 8: Baseline scan reads git-tracked Swift files
`baseline` greps capitalised identifiers in `git ls-files 'iosApp/**/*.swift'`. New test files are scanned too; today they reference no exported Kotlin names, so `referenced-names.txt` and `members-baseline.txt` are unchanged (verified: only `shared-h-baseline.txt` differed after regeneration). Phase 8 tests that reference Kotlin types will require re-baselining. Regenerate **after** the new test files are `git add`ed.

### Pitfall 9: Dependabot bumps the Gradle half only
`dependabot.yml` has only `package-ecosystem: gradle`, so a future `kmpNativeCoroutines` bump PR touches `libs.versions.toml` only; the new gate will fail it until the pbxproj pin is edited by hand. That is the intended behaviour; document the manual step in `documentation/DEVELOPMENT.md`.

### Pitfall 10: Firebase and other SPM deps float
`Package.resolved` is untracked and Firebase is `upToNextMajorVersion 11.14.0`, so CI resolves the latest 11.x on every cold run. Out of scope; the KMP-NC pin is `exactVersion` so it is unaffected. Do not "fix" by un-ignoring `*.xcworkspace` in this phase.

### Pitfall 11: KMP-NC runtime behaviour note for the smoke test
`1.0.0` release notes include "Fixed issue where `AsyncSequence` might not cancel `Flow` collection upon deallocation" `[CITED: gh release v1.0.0]`. This is the only Swift-runtime-side change between ALPHA-45 and 1.0.6 (ALPHA-46/47/48 and 1.0.1-1.0.6 notes list only Kotlin updates / Kotlin-side fixes `[CITED: gh release notes]`). The Swift product names are unchanged (`KMPNativeCoroutinesCore/Combine/Async/RxSwift`, `Package.swift` of `v1.0.6`: `swift-tools-version:5.3`, `platforms: [.iOS(.v13), ...]`). Relevant for the D-11 smoke (agenda observation, filters, favorite toggle, relaunch).

## Code Examples

### A. iOSApp guard (verified compile + host launch)
```swift
@main
struct iOSApp: App {
    /// True when the process is hosting an XCTest / Swift Testing run.
    static let isRunningTests =
        ProcessInfo.processInfo.environment["XCTestConfigurationFilePath"] != nil

    init() {
        if Self.isRunningTests { return }
        // ... existing body unchanged: FirebaseApp.configure(), IosDependencies.shared.start(),
        // PerformanceMonitoring traces, _ = RCValues.sharedInstance
    }

    var body: some Scene {
        WindowGroup {
            if Self.isRunningTests {
                Color.clear
            } else {
                ContentView()
                    .preferredColorScheme(.dark)
            }
        }
    }
}
```
Production path is byte-for-byte the same calls in the same order (the env var is absent outside XCTest). Note the existing file also declares `var remoteConfig = RemoteConfig.remoteConfig()` at file scope; Swift globals in non-main files are lazy, so it is not touched under test (verified implicitly: host launched and tests passed).

### B. SessionFilter tests (verified shape; names are snake_case sentences per project rule)
```swift
import Testing
import Foundation
@testable import DevFest_Nantes

@Suite struct SessionFilterTests {
    @Test func decodes_legacy_agenda_session_filters_payload() throws {
        let json = #"[{"type":"room","value":"Salle A"},{"type":"bookmark","value":"true"}]"#
        let filters = try JSONDecoder().decode(Set<SessionFilter>.self, from: Data(json.utf8))
        #expect(filters == [SessionFilter(type: .room, value: "Salle A"),
                            SessionFilter(type: .bookmark, value: "true")])
    }
    @Test(arguments: [FilterType.bookmark, .complexity, .language, .room, .type])
    func round_trips_every_filter_type(_ type: FilterType) throws {
        let original: Set<SessionFilter> = [SessionFilter(type: type, value: "x")]
        let data = try JSONEncoder().encode(original)
        #expect(try JSONDecoder().decode(Set<SessionFilter>.self, from: data) == original)
    }
    @Test func rejects_unknown_filter_type() {
        let json = #"[{"type":"track","value":"x"}]"#
        #expect(throws: DecodingError.self) {
            try JSONDecoder().decode(Set<SessionFilter>.self, from: Data(json.utf8))
        }
    }
}
```
(The decode test and `@testable import DevFest_Nantes` were run for real; the parameterised and negative cases follow the same API. A parameterised test counts as **one** test in `totalTestCount`, verified: 2 tests for a plain + a 3-argument parameterised test.)

For `getDate(date:)`: it is an instance method on `AgendaViewModel` (`Agenda/AgendaViewModel.swift`): `func getDate(date: String) -> Date { let newFormatter = ISO8601DateFormatter(); return newFormatter.date(from: date) ?? Date() }`. To test it without Koin, extract to a `static`/free helper (parity-neutral, e.g. `AgendaViewModel.parseDate(_:)` called by `getDate`). Note the fallback `?? Date()` returns *now* for an invalid string - assert only the valid-ISO8601 case and (optionally) that invalid input yields a date close to now. Planner choice: include with extraction, or defer and ship `SessionFilter` tests only (D-01 allows both).

### C. Simulator resolution by UDID (verified locally)
```bash
read -r UDID NAME < <(xcrun simctl list devices available --json | jq -r '
  [ .devices | to_entries[]
    | select(.key | test("SimRuntime\\.iOS-"))
    | (.key | capture("iOS-(?<v>[0-9-]+)$").v | split("-") | map(tonumber)) as $rt
    | .value[]
    | select(.isAvailable == true and (.name | test("^iPhone [0-9]+$")))
    | {udid, name, rt: $rt, n: (.name | capture("^iPhone (?<n>[0-9]+)$").n | tonumber)} ]
  | sort_by([.rt, .n]) | last | "\(.udid) \(.name)"')
echo "udid=$UDID" >> "$GITHUB_OUTPUT"; echo "name=$NAME" >> "$GITHUB_OUTPUT"
xcrun simctl boot "$UDID" || true
xcrun simctl bootstatus "$UDID" -b
```
Locally returned `F59F5246-2D70-49BD-B6CF-82DD9A67BF48 iPhone 17` (iOS 27.0 runtime). `read` with a name containing a space: `NAME` captures "iPhone 17" because `read -r UDID NAME` assigns the remainder to the last variable. Guard: fail the step if `UDID` is empty.

### D. `scripts/xcresult-assert-tests.sh` (verified on a real 2-test bundle and a real 0-test bundle)
```bash
#!/usr/bin/env bash
# Fails when an .xcresult executed no tests (xcodebuild exits 0 on "0 tests").
# Usage: bash scripts/xcresult-assert-tests.sh <path-to.xcresult>
set -euo pipefail
XCRESULT="${1:?usage: $0 <path-to.xcresult>}"
[ -d "$XCRESULT" ] || { echo "XCRESULT-MISSING $XCRESULT"; exit 1; }
SUMMARY="$(xcrun xcresulttool get test-results summary --path "$XCRESULT" --compact)"
TOTAL="$(printf '%s' "$SUMMARY" | jq -r '.totalTestCount // 0')"
PASSED="$(printf '%s' "$SUMMARY" | jq -r '.passedTests // 0')"
FAILED="$(printf '%s' "$SUMMARY" | jq -r '.failedTests // 0')"
SKIPPED="$(printf '%s' "$SUMMARY" | jq -r '.skippedTests // 0')"
echo "XCRESULT-TESTS total=$TOTAL passed=$PASSED failed=$FAILED skipped=$SKIPPED"
if [ "$TOTAL" -lt 1 ] || [ "$PASSED" -lt 1 ]; then echo "XCRESULT-NO-TESTS-EXECUTED"; exit 1; fi
if [ "$FAILED" -gt 0 ]; then echo "XCRESULT-TESTS-FAILED"; exit 1; fi
echo "XCRESULT-OK"
```
Observed: 2-test bundle -> `XCRESULT-TESTS total=2 passed=2 failed=0 skipped=0` / `XCRESULT-OK` / rc 0; 0-test bundle -> `XCRESULT-NO-TESTS-EXECUTED` / rc 1. Summary keys available: `passedTests, failedTests, skippedTests, expectedFailures, totalTestCount, result, devicesAndConfigurations, ...`. For `$GITHUB_STEP_SUMMARY`, append the `XCRESULT-TESTS` line plus `xcodebuild -version` and the simulator name.

### E. `scripts/kmp-nc-version-gate.sh` (verified on: current main = SKEW, bumped = OK, `kind` changed = NOT-EXACT)
```bash
#!/usr/bin/env bash
# KMP-NativeCoroutines version-skew gate (CONC-03).
# Usage: bash scripts/kmp-nc-version-gate.sh   (env overrides: KMP_NC_TOML, KMP_NC_PBXPROJ)
set -euo pipefail
export LC_ALL=C
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
TOML="${KMP_NC_TOML:-${REPO_ROOT}/gradle/libs.versions.toml}"
PBXPROJ="${KMP_NC_PBXPROJ:-${REPO_ROOT}/iosApp/iosApp.xcodeproj/project.pbxproj}"

GRADLE_VERSION="$(sed -nE 's/^kmpNativeCoroutines[[:space:]]*=[[:space:]]*"([^"]+)".*/\1/p' "$TOML" | head -1)"
if [ -z "$GRADLE_VERSION" ]; then echo "KMPNC-GRADLE-VERSION-MISSING $TOML"; exit 1; fi

BLOCK="$(awk '
  /isa = XCRemoteSwiftPackageReference;/ { inref = 1; buf = ""; hit = 0 }
  inref { buf = buf $0 "\n"; if ($0 ~ /repositoryURL = .*KMP-NativeCoroutines/) hit = 1 }
  inref && /^\t\t};/ { if (hit) printf "%s", buf; inref = 0 }
' "$PBXPROJ")"
if [ -z "$BLOCK" ]; then echo "KMPNC-SPM-PIN-MISSING $PBXPROJ"; exit 1; fi
KIND="$(printf '%s' "$BLOCK" | sed -nE 's/^[[:space:]]*kind = ([A-Za-z]+);.*/\1/p' | head -1)"
SPM_VERSION="$(printf '%s' "$BLOCK" | sed -nE 's/^[[:space:]]*version = "?([^";]+)"?;.*/\1/p' | head -1)"

FAIL=0
if [ "$KIND" != "exactVersion" ]; then echo "KMPNC-SPM-NOT-EXACT kind=${KIND:-<none>} (expected exactVersion)"; FAIL=1; fi
if [ "$SPM_VERSION" != "$GRADLE_VERSION" ]; then echo "KMPNC-VERSION-SKEW gradle=$GRADLE_VERSION spm=${SPM_VERSION:-<none>}"; FAIL=1; fi
[ "$FAIL" -eq 0 ] || exit 1
echo "KMPNC-VERSION-OK $GRADLE_VERSION"
```
Inputs it parses, quoted from the repo:
- `gradle/libs.versions.toml:23`: `kmpNativeCoroutines = "1.0.6"` `[VERIFIED]`; plugin alias line 144: `kmp-native-coroutines = { id = "com.rickclephas.kmp.nativecoroutines", version.ref = "kmpNativeCoroutines" }`.
- `project.pbxproj:921-928`:
  ```
  003EC8D72DF8D84B000E5689 /* XCRemoteSwiftPackageReference "KMP-NativeCoroutines" */ = {
  	isa = XCRemoteSwiftPackageReference;
  	repositoryURL = "https://github.com/rickclephas/KMP-NativeCoroutines.git";
  	requirement = {
  		kind = exactVersion;
  		version = "1.0.0-ALPHA-45";
  	};
  };
  ```
  The edit is the single line `version = "1.0.0-ALPHA-45";` -> `version = "1.0.6";`. Phase 11 (`Package.swift`): add a second source by extending the script (e.g. grep `exact: "x"` in the `Package.swift`), keeping the same exit/message contract.

### F. Workflow fragments (shape; keep existing steps otherwise)
```yaml
      - name: KMP-NativeCoroutines version gate
        run: bash scripts/kmp-nc-version-gate.sh          # right after checkout, before android-setup

      - name: Select latest installed Xcode
        id: xcode
        run: |
          LATEST_XCODE=$(ls -d /Applications/Xcode_*.app 2>/dev/null | sed -E 's#.*/Xcode_([0-9.]+)\.app#\1#' | sort -V | tail -1)
          sudo xcode-select -s "/Applications/Xcode_${LATEST_XCODE}.app/Contents/Developer"
          echo "version=$(xcodebuild -version | awk 'NR==1{v=$2} NR==2{b=$3} END{print v "-" b}')" >> "$GITHUB_OUTPUT"

      - name: Cache Xcode DerivedData          # MOVED after Xcode selection
        uses: actions/cache@v4
        with:
          path: |
            ~/Library/Developer/Xcode/DerivedData
            build
          key: ${{ runner.os }}-xcode-${{ steps.xcode.outputs.version }}-deriveddata-${{ hashFiles('iosApp/iosApp.xcodeproj/project.pbxproj') }}-${{ hashFiles('shared/**/*.kt', 'core/**/*.kt') }}
          restore-keys: |
            ${{ runner.os }}-xcode-${{ steps.xcode.outputs.version }}-deriveddata-${{ hashFiles('iosApp/iosApp.xcodeproj/project.pbxproj') }}-
            ${{ runner.os }}-xcode-${{ steps.xcode.outputs.version }}-deriveddata-

      # ... Build shared framework (unchanged) ...
      - name: Swift names gate
        run: |
          export SWIFT_GATE_HEADER="$PWD/shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework/Headers/shared.h"
          bash scripts/swift-names-gate.sh check
          bash scripts/swift-names-gate.sh selftest
      # ... KMP iOS tests, SwiftGen, mock plist, resolve packages (unchanged) ...
      - name: Build for testing
        run: xcodebuild ... build-for-testing ...            # Pattern 2 command
      - name: Run tests
        run: |
          rm -rf "$RUNNER_TEMP/ios-tests.xcresult"
          xcodebuild ... -resultBundlePath "$RUNNER_TEMP/ios-tests.xcresult" -parallel-testing-enabled NO test-without-building
      - name: Assert tests executed
        run: bash scripts/xcresult-assert-tests.sh "$RUNNER_TEMP/ios-tests.xcresult" | tee -a "$GITHUB_STEP_SUMMARY"
      - name: Upload xcresult
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: ios-xcresult
          path: ${{ runner.temp }}/ios-tests.xcresult
```
Notes: `ios.yml` has no `paths:` filter, so the gate step runs on every PR including Dependabot Gradle bumps `[VERIFIED: ios.yml triggers]`. The assertion step must run even when "Run tests" failed? No: a failed test step already fails the job; run the assertion only after success. The step-summary lines (Xcode version, simulator name, count) go in one extra `run:` step writing to `$GITHUB_STEP_SUMMARY` (D-06). Piping `bash script | tee` is acceptable because the shell for `run:` has `-e` and the script's own exit code is what matters only if `pipefail` is on; set `shell: bash` (which implies `-eo pipefail`) on that step, or write the script output to a variable instead of piping.

### G. swift-names-gate restoration (verified)
Source: `.planning/milestones/v1.0-phases/03-multi-module-architecture-extraction/swift-names-gate.sh` (14.7K). Required edits, all verified by running `check`, `baseline`, `selftest` in a scratch copy:
1. `REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"` -> `REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"`
2. `BASELINE_FILE="${SCRIPT_DIR}/03-shared-h-baseline.txt"` -> `${SCRIPT_DIR}/swift-names/shared-h-baseline.txt`
3. `REFERENCED_FILE=…/03-swift-referenced-names.txt` -> `${SCRIPT_DIR}/swift-names/referenced-names.txt`
4. `MEMBERS_FILE=…/03-swift-members-baseline.txt` -> `${SCRIPT_DIR}/swift-names/members-baseline.txt`
5. `EXCLUDE_FILE=…/03-swift-names-exclude.txt` -> `${SCRIPT_DIR}/swift-names/names-exclude.txt`
6. Header comment mentions the `03-*.txt` names: update the text (modes list, baseline description).
Baseline files are moved/renamed from the same archive directory (`03-swift-names-exclude.txt`, `03-swift-referenced-names.txt`, `03-swift-members-baseline.txt`, `03-shared-h-baseline.txt`). Observed results on the current `shared.h`:
- `check` with the **archived** baselines: `SWIFT-NAMES-OK` (informational diff lists newly exported fixtures such as `alice`, `bruno`, `PartnerFixtures`; no failure).
- After `baseline` regeneration (D-08): 400 names, 21 referenced names, 216 member pairs; `referenced-names.txt`, `members-baseline.txt`, `names-exclude.txt` identical to archived; only `shared-h-baseline.txt` changed (e.g. removed `MAX_PARTNERS`, `MAX_ROOMS`, `MAX_SESSIONS`, `MAX_SPEAKERS`, `MAX_SPEAKER_PER_SESSION`; added `PartnerFixtures`). Exclusions kept verbatim: `Link` and `SpeakerDetails` with their reasons.
- `selftest`: `SWIFT-GATE-SELFTEST-OK` in 0.7 s with `SWIFT_GATE_HEADER` set.
Success criterion 3 mapping: renaming/removing an exported referenced type -> `SWIFT-NAME-MISSING <name>`; swapped identity -> `SWIFT-MEMBERS-CHANGED <Type>`; new `_` collision -> `SWIFT-TYPE-COLLISION` / `SWIFT-NAME-COLLISION`. Selftest proves the last two still fire.

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| XCTest-only targets | Swift Testing (`@Test`, `#expect`) alongside XCTest in the same bundle | Xcode 16 (2024) | New target uses Swift Testing only; `Executed 0 tests` from the XCTest half is expected noise |
| `xcresulttool get --format json` (legacy) | `xcresulttool get test-results summary/tests` | Xcode 16 | Use the new API (verified on 27.1) |
| KMP-NC with KSP, `1.0.0-ALPHA-N` | KMP-NC 1.0.x, KSP support removed in 1.0.0, versions without Kotlin suffix for stable | 1.0.0 (2025-12-23) | Gradle already on 1.0.6; only SPM lagged `[CITED: gh release v1.0.0]` |
| macOS runner default | `macos-latest` = macOS 26 arm64, default Xcode 26.6, simulator runtimes iOS 26.2/26.4/26.5 | 2026 | The v1.0 "latest `Xcode_*.app`" selection resolves to 26.6 today `[CITED: runner-images]` |

**Deprecated/outdated:** the `DevFest NantesTests` target (references a non-existent `DevFestViewModel`, stale CocoaPods phase) - removed in this phase.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `xcresulttool get test-results summary` behaves the same on Xcode 26.6 (CI default) as on the locally verified 27.1 | Code Examples D | Assertion step fails on CI; mitigated by running the first CI iteration and keeping the script a single place to adjust. The API exists since Xcode 16 `[ASSUMED]` |
| A2 | `XCTestConfigurationFilePath` is set for hosted Swift Testing runs on Xcode 26.6 as observed on 27.1 | Pattern 3 | Guard would not trigger on CI, host would start Firebase/Koin. Cheap fallback: also OR `NSClassFromString("XCTestCase") != nil` (also observed). `[ASSUMED]` for 26.6 |
| A3 | `actions/upload-artifact@v4` is still supported | Supporting | Step warning/failure; bump to the current major (v7.0.2 is latest `[VERIFIED]`) |
| A4 | Cold CI timing stays under the 30 min job timeout with the added `build-for-testing`+tests (local cold Gradle+Xcode build ~3 min, tests ~40 s on a warm simulator) | CI structure | D-04 allows a split only if measured; measure on first PR run `[ASSUMED]` |
| A5 | In-place rewrite of the old target vs delete+recreate are equivalent for D-03 | Alternatives | If the user wants a fresh blueprint ID/history, use delete+create; no functional difference |

## Open Questions

1. **Include `getDate` in the first test set?**
   - Known: not testable without an `AgendaViewModel` instance, which self-starts Koin.
   - Unclear: whether the user accepts a small extraction (parity-neutral) for testability.
   - Recommendation: do the extraction as its own tiny commit (static helper + `getDate` delegating) and test it; otherwise ship `SessionFilter` tests only (D-01 explicitly allows).
2. **Code coverage in the plan.** The old scheme had `codeCoverageEnabled = "YES"`. The plan above sets `codeCoverage: false` (faster, nothing consumes coverage). Planner may keep it on; the scheme-level attribute is ignored once a plan is the default.
3. **`xcodebuild -resolvePackageDependencies` placement/caching.** Keep as today, or add a separate SPM cache via `-clonedSourcePackagesDirPath` (discretion item). Not required for the success criteria.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Xcode / xcodebuild | all iOS verification | yes (local) | 27.1 (27A9275); CI: 26.6 default | - |
| iOS simulator runtime | tests | yes (local) | iOS 18.0/18.3/18.6/26.1/27.0; CI: 26.2/26.4/26.5 | - |
| jq | simulator resolution, xcresult script | yes | local present; CI 1.8.2 | - |
| swiftgen | app build phase | yes (local `/opt/homebrew/bin/swiftgen`); CI installs via brew | - | - |
| gh CLI | release/tag facts | yes | - | - |
| Ruby `xcodeproj` gem | pbxproj editing | no | - | Python/sed edit + `plutil -lint` + `xcodebuild -list` (used in the prototype) |
| xcbeautify | pretty logs | no locally; present on CI | CI 3.2.1 | skip (not needed) |
| Gradle / Kotlin Native toolchain | framework build, header for the gate | yes (warm caches; scratch build of the app incl. Gradle phase finished in ~3 min) | Kotlin 2.4.21 | - |

**Missing dependencies with no fallback:** none.
**Missing dependencies with fallback:** `xcodeproj` gem -> scripted text edit.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | Swift Testing (toolchain, Testing Library 2084 on Xcode 27.1) in a hosted unit-test bundle; bash gates with built-in `selftest` |
| Config file | `iosApp/iosApp.xctestplan` (new), scheme `iosApp.xcscheme` |
| Quick run command | `bash scripts/kmp-nc-version-gate.sh && SWIFT_GATE_HEADER=shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework/Headers/shared.h bash scripts/swift-names-gate.sh check` |
| Full suite command | `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -testPlan iosApp -destination 'platform=iOS Simulator,id=<UDID>' -derivedDataPath build build-for-testing CODE_SIGNING_ALLOWED=NO` then the same with `-resultBundlePath <path> -parallel-testing-enabled NO test-without-building`, then `bash scripts/xcresult-assert-tests.sh <path>` |

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| IOSTEST-03 | Swift Testing target runs >= 1 real test green via plan | integration (simulator) | full suite command above | Wave 0 (new target/plan/tests) |
| IOSTEST-03 | Job fails on 0 executed tests | script check | `bash scripts/xcresult-assert-tests.sh <zero-test xcresult>` expect rc 1 | Wave 0 (new script; fixture produced by `-only-testing:` a nonexistent test) |
| IOSTEST-03 | Cache key contains Xcode version | static review | `grep -n "steps.xcode.outputs.version" .github/workflows/ios.yml` shows it in key and both restore-keys | Wave 0 |
| IOSTEST-04 | A changed Swift name of an exported type fails the gate | gate selftest + check | `bash scripts/swift-names-gate.sh selftest` -> `SWIFT-GATE-SELFTEST-OK`; `check` -> `SWIFT-NAMES-OK` | Wave 0 (restored script + baselines) |
| CONC-03 | Gradle and SPM versions equal and exact | script | `bash scripts/kmp-nc-version-gate.sh` -> `KMPNC-VERSION-OK 1.0.6`; negative: run with `KMP_NC_PBXPROJ` pointing to a modified copy -> rc 1 | Wave 0 |
| CONC-03 | App behaves as in v1.0 after the bump | manual checkpoint (D-11) | simulator smoke: Agenda (sessions, filters, favorite add/remove, relaunch), session detail, Speakers, Venue, About, screenshots | manual-only (UI/network, no automated equivalent in this phase) |

### Sampling Rate
- **Per task commit:** the relevant gate(s) / `xcodebuild -list` + `plutil -lint` after pbxproj edits.
- **Per wave merge:** full suite command (build-for-testing + test-without-building + assertion).
- **Phase gate:** iOS CI job green on a PR, including a deliberate-failure demonstration for the three guards (zero tests, Swift name change via `SWIFT_GATE_HEADER` doctored header or selftest, version skew) before `/gsd-verify-work`.

### Wave 0 Gaps
- [ ] `iosApp/DevFestNantesTests/SessionFilterTests.swift` (+ new target in pbxproj) - covers IOSTEST-03
- [ ] `iosApp/iosApp.xctestplan` + scheme `TestPlans` - covers IOSTEST-03
- [ ] `scripts/xcresult-assert-tests.sh` - covers IOSTEST-03 (D-05)
- [ ] `scripts/swift-names-gate.sh` + `scripts/swift-names/*` - covers IOSTEST-04
- [ ] `scripts/kmp-nc-version-gate.sh` - covers CONC-03
- Framework install: none (toolchain provided).

## Security Domain

`security_enforcement` is enabled (ASVS level 1, block on high). The phase is CI/test infrastructure with no user data, auth, or network surface added.

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | - |
| V3 Session Management | no | - |
| V4 Access Control | no | - |
| V5 Input Validation | limited | Scripts only parse repo files; quote all expansions, `set -euo pipefail`, no `eval`; `SWIFT_GATE_HEADER`/`KMP_NC_*` env overrides are developer/CI-controlled |
| V6 Cryptography | no | - |
| V14 Configuration (build/CI) | yes | Third-party Actions pinned by major tag as elsewhere in the repo; no secrets used (mock `GoogleService-Info.plist` already generated in CI); artifact upload contains only a test result bundle |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Supply-chain: SPM version drift | Tampering | `exactVersion` pin enforced by `kmp-nc-version-gate.sh` (fails on non-exact pin) |
| Poisoned/stale CI cache | Tampering | Xcode-aware cache keys; hash of pbxproj + Kotlin sources |
| Test-only code path shipping in production | Tampering / EoP | Guard keyed on `XCTestConfigurationFilePath`, absent in production; the production branch is unchanged code; no test hooks exposed |
| Secrets in test artifacts | Information disclosure | `.xcresult` from a build using a mock Firebase plist and no secrets; upload only on failure |
| Shell injection in workflow | Tampering | No `${{ }}` interpolation of untrusted PR data into `run:` (only step outputs from our own steps) |

## Sources

### Primary (HIGH confidence)
- Local prototype in a scratch copy of the repo (Xcode 27.1, iOS 27 simulator): hosted Swift Testing target, `.xctestplan` + scheme, `build-for-testing`/`test-without-building`, `-parallel-testing-enabled NO`, `xcresulttool get test-results summary`, zero-test behaviour, KMP-NC 1.0.6 resolve + compile + test, restored `swift-names-gate.sh` (`check`/`baseline`/`selftest`), draft gate scripts
- Repo files read this session: `.github/workflows/ios.yml`, `android.yml`, `iosApp/iosApp.xcodeproj/project.pbxproj` and `xcshareddata/xcschemes/iosApp.xcscheme`, `iosApp/iosApp/iOSApp.swift`, `Model/SessionFilter.swift`, `Model/BaseViewModel.swift`, `Agenda/AgendaViewModel.swift`, `UI/ContentView.swift`, `shared/.../IosDependencies.kt`, `gradle/libs.versions.toml`, `.github/dependabot.yml`, `.planning/milestones/v1.0-phases/03-multi-module-architecture-extraction/swift-names-gate.sh` and baselines, `scripts/di-gate.sh`
- `git ls-remote` / `gh release` on `rickclephas/KMP-NativeCoroutines` (tags `v1.0.6` -> `54a45fbb15bc0bebc54081f87683830d3bc9e84e`, release notes ALPHA-46 ... 1.0.6, `Package.swift` of `v1.0.6`, README SPM section)

### Secondary (MEDIUM confidence)
- github.com/actions/runner-images README and `macos-26-arm64-Readme.md` (macos-latest = macOS 26 arm64; Xcode 26.6 default; runtimes iOS 26.2/26.4/26.5; jq 1.8.2; xcbeautify 3.2.1)
- gh release list for `actions/upload-artifact`, `actions/cache`, `actions/checkout` (latest majors)

### Tertiary (LOW confidence)
- none

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - toolchain-only, versions read from the registry/tags and the local toolchain
- Architecture: HIGH - the whole lane was run end to end on the real project (modified copy)
- Pitfalls: HIGH for the verified ones (zero-test exit 0, host-app startup, cache ordering, `-derivedDataPath` flag); MEDIUM for CI-only items (Xcode 26.6 vs 27.1 differences, timing) listed in the Assumptions Log

**Research date:** 2026-10-26
**Valid until:** ~2026-11-25 (stable tooling; re-check runner image Xcode if `macos-latest` changes)
