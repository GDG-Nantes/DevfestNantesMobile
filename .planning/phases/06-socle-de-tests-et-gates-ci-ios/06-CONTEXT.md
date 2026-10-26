# Phase 6: Socle de tests et gates CI iOS - Context

**Gathered:** 2026-10-26
**Status:** Ready for planning

<domain>
## Phase Boundary

Chaque PR exécute réellement des tests Swift sur la CI iOS (`ios.yml`) et les gates d'interop
Kotlin/Swift sont actifs : nouvelle target Swift Testing qui remplace `DevFest NantesTests`, lane
`build-for-testing` / `test-without-building` pilotée par un `.xctestplan`, échec si 0 test,
cache Xcode-aware, `swift-names-gate.sh` restauré et branché en CI, KMP-NativeCoroutines aligné
en 1.0.6 côté SPM avec un gate de version. Exigences : IOSTEST-03, IOSTEST-04, CONC-03.
Hors périmètre : tests de ViewModels sur fakes (Phase 8, IOSTEST-01), smoke tests SwiftUI
(Phase 12), iOS 17 partout (Phase 8), tests des packages SPM (Phase 11).

</domain>

<decisions>
## Implementation Decisions

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

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Périmètre et exigences
- `.planning/ROADMAP.md` §Phase 6 — goal, 4 critères de succès, règles transverses v1.1
- `.planning/REQUIREMENTS.md` — IOSTEST-03, IOSTEST-04, CONC-03 (+ note : target obsolète
  remplacée dès la Phase 6)
- `.planning/PROJECT.md` — core value, parité stricte

### Recherche v1.1
- `.planning/research/SUMMARY.md` — séquencement, ligne « Test lane » et phase « tests + gates »
- `.planning/research/PITFALLS.md` — Pitfall 9 (noms Swift / gate), 13 (Swift Testing, bundle
  hébergé), 14 (CI iOS : cache, 0 test, simulateur), 17 (skew KMP-NC), 18 (deployment targets)
- `.planning/research/STACK.md` — Swift Testing toolchain, KMP-NC 1.0.6

### Gates archivés v1.0
- `.planning/milestones/v1.0-phases/03-multi-module-architecture-extraction/swift-names-gate.sh`
  — script à restaurer (modes baseline/check/selftest, env `SWIFT_GATE_HEADER`)
- `.planning/milestones/v1.0-phases/03-multi-module-architecture-extraction/03-swift-names-exclude.txt`,
  `03-swift-referenced-names.txt`, `03-swift-members-baseline.txt`, `03-shared-h-baseline.txt`
  — baselines à déplacer/regénérer
- `scripts/di-gate.sh` — modèle de gate existant (style, sortie, usage CI)

### Codebase
- `.planning/codebase/TESTING.md`, `.planning/codebase/CONCERNS.md`
- `AGENTS.md` §iOS — commandes de build iOS, SwiftGen, plist non commité

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `.github/workflows/ios.yml` : étapes existantes à conserver (sélection dynamique Xcode,
  résolution simulateur via `jq`, build du framework `embedAndSignAppleFrameworkForXcode`,
  tests KMP `iosSimulatorArm64Test`, SwiftGen, plist factice, `-resolvePackageDependencies`).
- `swift-names-gate.sh` archivé : lie le framework debug `iosSimulatorArm64` de `:shared` et
  lit `Headers/shared.h` ; `SWIFT_GATE_HEADER` permet de rejouer un en-tête sans rebuild.
- `iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/iosApp.xcscheme` : scheme partagé avec un
  `TestAction` à brancher sur le `.xctestplan`.

### Established Patterns
- Gates en bash dans `scripts/` exécutés par la CI (`di-gate.sh`), sortie à lignes
  préfixées et `*-OK` en succès.
- Sélection dynamique de Xcode et du simulateur (décision v1.0) : ne pas épingler de version.
- Tests : noms en phrases, pas de lib de mock ; Swift Testing de la toolchain uniquement.

### Integration Points
- `iosApp/iosApp/iOSApp.swift` : `init()` à neutraliser sous test (Firebase, Koin, perf, RC).
- `iosApp/iosApp.xcodeproj/project.pbxproj` : suppression de l'ancienne target, ajout de la
  nouvelle, pin `XCRemoteSwiftPackageReference "KMP-NativeCoroutines"`
  (`exactVersion 1.0.0-ALPHA-45` → `1.0.6`) ; `Package.resolved` à mettre à jour.
- `gradle/libs.versions.toml` : `kmpNativeCoroutines = "1.0.6"` (source de vérité du gate).
- Deployment targets mixtes (15.0 ×4, 17.0 ×2) et `SWIFT_VERSION = 5.0` dans le pbxproj.

</code_context>

<specifics>
## Specific Ideas

- Les tests de `SessionFilter` servent de garde-fou au format persisté que la Phase 9 doit
  relire sans migrateur.
- Smoke KMP-NC calqué sur les UAT simulateur de v1.0 (captures).

</specifics>

<deferred>
## Deferred Ideas

- `resources-gate.sh` (v1.0 Phase 3) : preuve de migration de ressources **Android** one-shot,
  inutilisable telle quelle. À reconsidérer sous forme de gate iOS (`en.lproj`/`fr.lproj`
  `Localizable.strings` aux clés identiques) en Phase 11 avec les ressources SwiftGen en
  packages, ou en invariant Android permanent dans une phase dédiée.
- Seuil minimum de tests configurable (au-delà de « 0 test = échec ») : à revoir quand les
  Phases 8/12 auront ajouté des tests.
- Découpage du job iOS en plusieurs jobs : seulement si le timeout de 30 min est dépassé
  (naturellement reconsidéré en Phase 11 avec l'XCFramework).

</deferred>

---

*Phase: 06-socle-de-tests-et-gates-ci-ios*
*Context gathered: 2026-10-26*
