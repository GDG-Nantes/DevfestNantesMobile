# Roadmap: DevFest Nantes App Modernization

## Milestones

- ✅ **v1.0 Modernization**: Phases 1-5 (shipped 2026-10-16). See [archive](milestones/v1.0-ROADMAP.md).
- 🚧 **v1.1 iOS Architecture**: Phases 6-12 (in progress)

## Overview

v1.1 amène l'app iOS au niveau de l'app Android sans rien changer à ce que voient les utilisateurs. Le milestone commence par une vraie lane de tests iOS en CI et par les gates d'interop Kotlin/Swift, pour que chaque refonte suivante dispose d'un signal fiable. La logique métier dupliquée (filtrage, regroupement de l'agenda, classification, favoris et filtres) part ensuite dans un module KMP `:core:domain`, adopté d'abord par Android, dont les tests v1.0 servent d'oracle de parité. Côté iOS, les ViewModels passent à Observation sur un socle iOS 17, puis l'app bascule sur la logique partagée en réutilisant les clés `UserDefaults` legacy (aucun favori ni filtre perdu), avant que la DI ne devienne une composition root explicite. Le découpage en packages SPM locaux, l'étape structurelle la plus risquée, vient ensuite. Le milestone se termine par le passage en Swift 6 strict, un smoke test par feature et la documentation. Chaque phase laisse la CI verte et l'app livrable. Une seule déviation visible est acceptée : sur iOS, l'ordre des salles dans un créneau suit `RoomSortIndex`, comme sur Android.

## Phases

**Phase Numbering:**
- Phases entières (6, 7, 8…) : travail planifié du milestone, numérotation continue depuis v1.0
- Phases décimales (7.1, 7.2) : insertions urgentes (marquées INSERTED)

Les phases décimales s'intercalent entre leurs phases entières voisines, dans l'ordre numérique.

<details>
<summary>✅ v1.0 Modernization (Phases 1-5): SHIPPED 2026-10-16</summary>

- [x] Phase 1: CI Pipeline Fixed & Optimized (3/3 plans), completed 2026-09-17
- [x] Phase 2: Dependency & Build Tooling Upgrade (5/5 plans), completed 2026-09-19
- [x] Phase 3: Multi-Module Architecture Extraction (11/11 plans), completed 2026-09-27
- [x] Phase 4: Hilt to Koin DI Migration (7/7 plans), completed 2026-10-13
- [x] Phase 5: Test Coverage Retrofit (11/11 plans), completed 2026-10-16

Full details: [milestones/v1.0-ROADMAP.md](milestones/v1.0-ROADMAP.md). Requirements: [milestones/v1.0-REQUIREMENTS.md](milestones/v1.0-REQUIREMENTS.md). Audit: [milestones/v1.0-MILESTONE-AUDIT.md](milestones/v1.0-MILESTONE-AUDIT.md).

</details>

### 🚧 v1.1 iOS Architecture (In Progress)

**Milestone Goal:** Moderniser l'app iOS au niveau de l'app Android (logique métier partagée en KMP, app SwiftUI modulaire en Observation + Swift 6 strict, couverture de tests réelle) sans aucun changement de comportement utilisateur.

- [ ] **Phase 6: Socle de tests et gates CI iOS** - La CI iOS exécute de vrais tests Swift à chaque PR et les gates d'interop Kotlin/Swift sont actifs
- [ ] **Phase 7: Couche domaine KMP et adoption Android** - La logique agenda/filtres/favoris vit dans `:core:domain`, testée sur JVM et simulateur iOS, et Android l'utilise sans différence visible
- [ ] **Phase 8: SwiftUI moderne, iOS 17 et Observation** - iOS 17 partout, ViewModels `@Observable @MainActor` testés sur fakes, Combine supprimé
- [ ] **Phase 9: iOS sur la logique commune et persistance legacy** - iOS délègue filtrage, regroupement et favoris/filtres au domaine partagé sans perdre les données v1.0
- [ ] **Phase 10: DI iOS explicite** - Composition root `AppDependencies` et injection par constructeur ; singletons et service-locator supprimés
- [ ] **Phase 11: Modularisation SPM** - `shared` en XCFramework statique, packages core et feature locaux, target app réduite à une coquille
- [ ] **Phase 12: Swift 6 strict, smoke tests et documentation** - Swift 6 sans warning, interop Kotlin confinée et contrôlée, un smoke test par feature, documentation à jour

**Règles transverses v1.1 :**
- Chaque phase laisse la CI Android et iOS verte et l'app livrable sur les deux plateformes.
- Parité stricte : les bugs découverts en cours de route sont consignés en dette, pas corrigés. Seule déviation visible acceptée : l'ordre des salles dans un créneau sur iOS suit `RoomSortIndex` (SHARED-03).
- Toute documentation invalidée par une phase est mise à jour dans la même modification (règle `AGENTS.md`) ; la Phase 12 vérifie l'ensemble (DOC-01).
- Arbitrages de recherche retenus (`research/SUMMARY.md`) : clés `UserDefaults` legacy réutilisées sans migrateur, un seul manifeste SPM, les features n'importent jamais le framework Kotlin, `AppDependencies` vit dans CoreDI, les nouveaux packages naissent en Swift 6 et le code déplacé bascule package par package.

## Phase Details

### Phase 6: Socle de tests et gates CI iOS
**Goal**: Chaque PR exécute réellement des tests Swift sur la CI iOS et les gates d'interop Kotlin/Swift sont actifs, pour que chaque refonte iOS suivante dispose d'un signal fiable
**Depends on**: Phase 5 (v1.0 livrée)
**Requirements**: IOSTEST-03, IOSTEST-04, CONC-03
**Success Criteria** (what must be TRUE):
  1. Sur chaque PR, la CI iOS lance `xcodebuild` en build-for-testing puis test-without-building via un test plan `.xctestplan`, et une target Swift Testing fonctionnelle (qui remplace `DevFest NantesTests`, lequel référence un `DevFestViewModel` inexistant) exécute au moins un test réel au vert
  2. Le job iOS échoue quand le `.xcresult` compte zéro test exécuté, et la clé de cache iOS inclut la version de Xcode
  3. `scripts/swift-names-gate.sh` est restauré et tourne en CI : un changement qui modifie le nom Swift d'un type Kotlin exporté fait échouer le job
  4. KMP-NativeCoroutines est en 1.0.6 côté SPM (au lieu de 1.0.0-ALPHA-45) comme côté Gradle, un gate CI échoue si les deux versions divergent, et l'app iOS se comporte comme en v1.0
**Plans**: 6 plans
Plans:
- [ ] 06-01-PLAN.md — Target Swift Testing `DevFestNantesTests` hébergée par l'app, `.xctestplan`, garde XCTest dans `iOSApp`, premier test `SessionFilter` (tracer), puis suppression de l'ancienne target (commit séparé)
- [ ] 06-02-PLAN.md — Suite `SessionFilter` complète (format legacy), extraction `AgendaViewModel.parseDate` (commit dédié) et ses tests, doc des tests iOS
- [ ] 06-03-PLAN.md — Lane CI iOS : build-for-testing / test-without-building, échec si 0 test (`xcresult-assert-tests.sh`), cache Xcode-aware, simulateur par UDID
- [ ] 06-04-PLAN.md — `swift-names-gate.sh` restauré dans `scripts/` (baselines dans `scripts/swift-names/`), check + selftest en CI
- [ ] 06-05-PLAN.md — KMP-NativeCoroutines SPM en 1.0.6 et `kmp-nc-version-gate.sh` dans le même commit, docs des gates, commandes CI Android d'AGENTS.md vertes
- [ ] 06-06-PLAN.md — Smoke manuel de parité v1.0 sur simulateur après le bump (checkpoint humain bloquant)
**Research flag**: pattern standard, pas de spike

### Phase 7: Couche domaine KMP et adoption Android
**Goal**: La logique métier de l'agenda (filtrage, regroupement, classification, favoris et filtres) vit dans un module KMP `:core:domain` testé sur JVM et simulateur iOS, et Android l'utilise déjà sans aucune différence visible
**Depends on**: Phase 6 (le gate `swift-names-gate.sh` en CI couvre les nouveaux exports)
**Requirements**: SHARED-01, SHARED-02, SHARED-03, SHARED-04, SHARED-05, SHARED-06, SHARED-07
**Success Criteria** (what must be TRUE):
  1. `:core:domain` (convention plugin `devfest.*`) est exporté par `:shared` ; son `domainModule` fait partie de `sharedModules`, `verify()` passe sans doublon de binding, `swift-names-gate.sh` passe sur les nouveaux exports et l'app iOS compile et se comporte comme avant
  2. Des tests de caractérisation (matrice de filtres v1.0, regroupement sous au moins deux fuseaux, payloads legacy capturés, comportements actuels iOS et Android) sont commités avant chaque déplacement de logique ; les use cases partagés (filtrage OU dans un type / ET entre types / favoris par appartenance, regroupement jour/créneau avec tri `RoomSortIndex`, classification service/navigable, index A-Z des speakers, langue du lieu) les passent en `commonTest` sur JVM et simulateur iOS
  3. Les comportements divergents (créneau courant au scroll, catégories de partenaires, fuseau des jours) restent par plateforme ou passent par un paramètre explicite, et la déviation d'ordre des salles côté iOS est documentée
  4. `BookmarksStore` et `SessionFiltersStore` sont des interfaces communes : les tests Robolectric v1.0 qui épinglent `selected_sessions` et le format des filtres Android passent sans modification, et l'implémentation iOS lit et écrit `Favorites` et `AgendaSessionFilters` au format legacy (tests sur payloads capturés, sur simulateur)
  5. Sur Android, l'agenda, les filtres, les favoris, les speakers et le lieu se comportent exactement comme en v1.0 (tests v1.0 verts + UAT agenda) ; `SessionFiltersService` a disparu et son binding Koin a été remplacé dans le même commit
**Plans**: TBD
**Research flag**: caractérisation à préparer : snapshot réel de l'API (110 speakers, ids de salles, vérifier `room.id == room.name`) en plus des stubs `core/model`, et forme réelle des payloads `Favorites`/`AgendaSessionFilters` capturée depuis une install v1.0 ou une fixture `Codable` Swift

### Phase 8: SwiftUI moderne, iOS 17 et Observation
**Goal**: Les ViewModels iOS passent au modèle Observation sur un socle iOS 17, sans Combine, avec une collecte liée au cycle de vie des vues et des tests unitaires sur fakes, sans changement visible
**Depends on**: Phase 6 (aucune dépendance technique à la Phase 7, exécutée après elle par ordre numérique)
**Requirements**: IOSUI-01, IOSUI-02, IOSUI-03, IOSTEST-01
**Success Criteria** (what must be TRUE):
  1. Toutes les targets iOS existantes déclarent un deployment target iOS 17.0 (plus de mélange 15.0/17.0) et l'app se build et tourne sur simulateur
  2. Les ViewModels Agenda, Speakers, SpeakerDetails, Venue et About sont `@Observable @MainActor` et possédés via `@State` : un re-render ne recrée pas le ViewModel, la collecte démarre dans `.task` et s'arrête quand la vue disparaît (aucun `Task {}` non structuré)
  3. Les sources iOS ne contiennent plus `import Combine`, `KMPNativeCoroutinesCombine`, `ObservableObject`, `@Published`, `objectWillChange.send()` ni `DispatchQueue.main.async`, et le produit SPM Combine est retiré du projet
  4. Chaque ViewModel iOS a des tests Swift Testing sur des fakes (ports étroits par ViewModel), exécutés dans la lane CI de la Phase 6
  5. UAT : agenda, filtres, favoris, détail de session, speakers, lieu et à propos se comportent comme en v1.0
**Plans**: TBD
**Research flag**: rejouer le pont async KMP-NativeCoroutines (`asyncSequence`/`asyncFunction`) sur la toolchain CI (Xcode 26.6 / Swift 6.3.3), pas seulement sur l'expérience locale en Swift 6.4
**UI hint**: yes

### Phase 9: iOS sur la logique commune et persistance legacy
**Goal**: L'app iOS délègue filtrage, regroupement et persistance des favoris et filtres à la couche domaine partagée, sans qu'aucun utilisateur ne perde ses favoris ou ses filtres à la mise à jour
**Depends on**: Phase 7, Phase 8
**Requirements**: IOSDATA-01, IOSDATA-02, IOSDATA-03
**Success Criteria** (what must be TRUE):
  1. Un utilisateur qui installe la nouvelle version par-dessus un build v1.0 (sessions en favoris, filtres actifs, app tuée puis relancée) retrouve tous ses favoris et filtres ; l'UAT de mise à jour en place et les tests sur payloads legacy capturés le confirment
  2. Les sources Swift ne filtrent, ne regroupent et ne persistent plus les favoris ni les filtres ; `SessionFilter`, `FilterType` et les autres types dupliqués sont supprimés au profit des types Kotlin exportés, et speakers, lieu et classification des sessions passent par les use cases partagés
  3. L'agenda iOS affiche les mêmes jours (libellés et fuseau), créneaux, sessions et résultats de filtres qu'en v1.0 (UAT comparatif), à la seule exception documentée de l'ordre des salles dans un créneau, qui suit désormais `RoomSortIndex`
  4. L'agenda observe en continu le flux combiné partagé (sessions + favoris + filtres) : ajouter ou retirer un favori ou un filtre depuis la liste, une cellule ou le détail met l'agenda à jour sans relance, via une seule instance d'`AgendaViewModel`
**Plans**: TBD
**Research flag**: spike + UAT, plus haut risque de perte de données du milestone : l'UAT de mise à jour en place exige un build release v1.0 installé (store, TestFlight ou tag v1.0) ; confirmer `room.id == room.name` pour le filtre de salles ; valider le flux combiné continu à travers le pont sur la toolchain CI
**UI hint**: yes

### Phase 10: DI iOS explicite
**Goal**: Les dépendances iOS sont composées une seule fois au lancement par une composition root explicite et injectées par constructeur, sans service-locator ni singleton global dans les ViewModels
**Depends on**: Phase 9
**Requirements**: IOSDI-01, IOSDI-02, IOSDI-03
**Success Criteria** (what must be TRUE):
  1. Au lancement, `AppDependencies.live()` est construit une seule fois (encore dans la target app à ce stade) et un test vérifie que l'ordre d'initialisation reste Firebase, consentement, performance puis valeurs par défaut Remote Config avant la première lecture
  2. Les ViewModels reçoivent leurs dépendances par constructeur via des protocoles ; `BaseViewModel`, `RCValues.sharedInstance`, le global `remoteConfig`, `PerformanceMonitoring.shared` et `FirebaseAnalyticsService.shared` n'existent plus
  3. `scripts/swift-di-gate.sh` tourne en CI et fait échouer le job dès qu'un fichier hors composition root accède à `IosDependencies` ou à un singleton
  4. Événements analytics, traces de performance, valeurs Remote Config et choix de consentement se comportent comme en v1.0 (UAT + tests sur fakes verts)
**Plans**: TBD
**Research flag**: pattern standard, sauf le test d'ordre d'initialisation

### Phase 11: Modularisation SPM
**Goal**: L'app iOS est découpée en packages SPM locaux core et feature qui consomment `shared` en XCFramework statique, avec une target app réduite à une coquille, et se build depuis un checkout propre en local comme en CI
**Depends on**: Phase 10
**Requirements**: IOSMOD-01, IOSMOD-02, IOSMOD-03
**Success Criteria** (what must be TRUE):
  1. Depuis un checkout propre, l'app se build en local et en CI avec `shared` produit par Gradle en XCFramework statique et consommé en `binaryTarget` SPM ; la phase `embedAndSignAppleFrameworkForXcode` n'existe plus
  2. Une archive Release conserve ses dSYMs, et un crash de test remonte symbolisé dans Crashlytics
  3. Le manifeste unique contient les targets core, toutes en `.iOS(.v17)` : pont Kotlin (seul importeur de `shared`), CoreServices, FirebaseServices, DesignSystem (sortie SwiftGen et ressources via `Bundle.module`) et CoreDI, qui héberge `AppDependencies`
  4. Agenda (détail de session inclus), Speakers, Venue et About sont des targets feature sans dépendance entre elles ni import du framework Kotlin ; la target app se contente d'appeler `AppDependencies.live()` et d'assembler les features, et les tests des packages tournent dans le test plan CI
  5. Après chaque extraction (About, Venue, Speakers puis Agenda, un commit chacune), un smoke manuel confirme que l'app se comporte comme en v1.0
**Plans**: TBD
**Research flag**: spike bloquant avant les extractions : ordre de build XCFramework depuis un checkout propre sur la CI (Xcode 26.6), rafraîchissement dans Xcode, `binaryTarget` hors racine du package, chemin Release/dSYM (`scripts/build-ios-release.sh`, `documentation/iOS_BUILD_DSYM.md`), support de `Bundle.module` par les templates SwiftGen 6.6.3 (sinon commiter la sortie générée), concurrence Firebase 11.15 à la frontière des packages
**UI hint**: yes

### Phase 12: Swift 6 strict, smoke tests et documentation
**Goal**: Tout le code iOS compile en Swift 6 strict sans warning, l'interop Kotlin est confinée et contrôlée, chaque feature a un smoke test et la documentation décrit la nouvelle architecture
**Depends on**: Phase 11
**Requirements**: CONC-01, CONC-02, IOSTEST-02, DOC-01
**Success Criteria** (what must be TRUE):
  1. Tous les packages et la target app compilent en mode de langage Swift 6 avec vérification complète de la concurrence et zéro warning en CI, et l'app se comporte comme en v1.0
  2. `asyncFunction`/`asyncSequence` et les conformances `@unchecked Sendable` (limitées aux types Kotlin immuables) n'apparaissent que dans le pont Kotlin ; un gate CI échoue si on en ajoute ailleurs
  3. Chaque feature (Agenda, Speakers, Venue, About) a un smoke test SwiftUI rendu via `UIHostingController`, exécuté en CI
  4. `README.md`, `documentation/ARCHITECTURE.md`, `documentation/DEVELOPMENT.md`, `documentation/iOS_BUILD_DSYM.md` et `AGENTS.md` décrivent les packages, la composition root, l'XCFramework et les commandes iOS, et un checkout propre se build en suivant uniquement ces documents
**Plans**: TBD
**Research flag**: rejouer le pont KMP-NativeCoroutines en Swift 6 sur la toolchain CI (Swift 6.3.3) ; si les diagnostics du pont sont nombreux, basculer package par package, jamais en un seul commit
**UI hint**: yes

## Progress

**Execution Order:**
Les phases s'exécutent dans l'ordre numérique : 6 → 7 → 8 → 9 → 10 → 11 → 12

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 1. CI Pipeline Fixed & Optimized | v1.0 | 3/3 | Complete | 2026-09-17 |
| 2. Dependency & Build Tooling Upgrade | v1.0 | 5/5 | Complete | 2026-09-19 |
| 3. Multi-Module Architecture Extraction | v1.0 | 11/11 | Complete | 2026-09-27 |
| 4. Hilt to Koin DI Migration | v1.0 | 7/7 | Complete | 2026-10-13 |
| 5. Test Coverage Retrofit | v1.0 | 11/11 | Complete | 2026-10-16 |
| 6. Socle de tests et gates CI iOS | v1.1 | 0/5 | Planned | - |
| 7. Couche domaine KMP et adoption Android | v1.1 | 0/? | Not started | - |
| 8. SwiftUI moderne, iOS 17 et Observation | v1.1 | 0/? | Not started | - |
| 9. iOS sur la logique commune et persistance legacy | v1.1 | 0/? | Not started | - |
| 10. DI iOS explicite | v1.1 | 0/? | Not started | - |
| 11. Modularisation SPM | v1.1 | 0/? | Not started | - |
| 12. Swift 6 strict, smoke tests et documentation | v1.1 | 0/? | Not started | - |
