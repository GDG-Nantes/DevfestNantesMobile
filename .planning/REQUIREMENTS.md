# Requirements: DevFest Nantes App Modernization — v1.1 iOS Architecture

**Defined:** 2026-10-23
**Core Value:** La CI/CD doit refonctionner et le projet doit redevenir maintenable (build moderne, architecture modulaire, DI décentralisée, couverture de tests solide) sans jamais régresser le comportement existant de l'application pour les utilisateurs.

## v1.1 Requirements

Moderniser l'app iOS au niveau de l'app Android : logique domaine partagée en KMP (UI natives conservées), app SwiftUI modulaire en Observation + Swift 6 strict, tests et CI iOS. Parité stricte de comportement, avec une seule déviation acceptée : l'ordre des salles dans un créneau sur iOS s'aligne sur Android (`RoomSortIndex`).

Contexte de recherche : `.planning/research/SUMMARY.md` (et STACK/FEATURES/ARCHITECTURE/PITFALLS).

### SHARED (Domaine KMP partagé)

- [ ] **SHARED-01**: Un module KMP `:core:domain` (convention plugin `devfest.*`, dépendant de `:core:model`/`:core:data`) est exporté par `:shared` ; son `domainModule` Koin est ajouté à `sharedModules` et vérifié par `verify()` sans doublon de binding
- [ ] **SHARED-02**: `SessionFilter`/`SessionFilterType` vivent dans `:core:model` ; la règle de filtrage des sessions (OU au sein d'un type, ET entre types, favoris = appartenance) est un use case partagé couvert par la matrice de filtres en `commonTest` (JVM + simulateur iOS)
- [ ] **SHARED-03**: Le regroupement de l'agenda par jour et par créneau, avec tri intra-créneau par `RoomSortIndex`, est un use case partagé ; la déviation d'ordre des salles côté iOS est documentée
- [ ] **SHARED-04**: Classification des sessions (service/navigable), index alphabétique des speakers et choix de langue du lieu sont partagés là où la sémantique est déjà identique sur les deux plateformes ; les comportements divergents (créneau courant au scroll, catégories de partenaires, fuseau des jours) restent par plateforme ou passent par un paramètre explicite
- [ ] **SHARED-05**: `BookmarksStore` et `SessionFiltersStore` sont des interfaces communes ; l'implémentation Android garde ses clés et son format SharedPreferences (`selected_sessions`, filtres), l'implémentation iOS s'appuie sur `NSUserDefaults` avec les clés existantes `Favorites` et `AgendaSessionFilters` lues et écrites au format legacy
- [ ] **SHARED-06**: Les ViewModels Android concernés consomment les use cases partagés et `SessionFiltersService` est retiré (binding Koin remplacé dans le même commit), sans régression (tests v1.0 verts + UAT agenda)
- [ ] **SHARED-07**: Des tests de caractérisation (payloads legacy capturés, regroupement sous au moins deux fuseaux, comportement actuel iOS et Android) existent avant chaque déplacement de logique

### IOSDATA (iOS sur la logique partagée)

- [ ] **IOSDATA-01**: L'app iOS ne filtre, ne regroupe et ne persiste plus les favoris/filtres en Swift ; les types Swift dupliqués (`SessionFilter`, `FilterType`…) sont supprimés au profit des types Kotlin exportés
- [ ] **IOSDATA-02**: Un utilisateur qui met à jour depuis la version v1.0 retrouve tous ses favoris et filtres (tests sur payloads legacy capturés + UAT de mise à jour en place depuis un build v1.0)
- [ ] **IOSDATA-03**: L'agenda iOS observe en continu le flux combiné partagé (sessions + favoris + filtres) ; liste, cellules et détail partagent une seule instance d'`AgendaViewModel`

### IOSUI (SwiftUI moderne)

- [ ] **IOSUI-01**: Toutes les targets et tous les packages iOS ont un deployment target iOS 17.0
- [ ] **IOSUI-02**: Tous les ViewModels iOS sont `@Observable @MainActor`, possédés via `@State` sans recréation au re-render, avec une collecte démarrée dans `.task` et annulée à la disparition de la vue (plus de `Task {}` non structuré)
- [ ] **IOSUI-03**: Combine, `KMPNativeCoroutinesCombine`, `ObservableObject`/`@Published`, `objectWillChange.send()` et `DispatchQueue.main.async` ne sont plus utilisés

### IOSDI (DI iOS explicite)

- [ ] **IOSDI-01**: Une composition root Swift (`AppDependencies.live()`) est construite une seule fois au lancement ; l'ordre d'initialisation actuel (Firebase, consentement, performance, valeurs par défaut Remote Config) est préservé et couvert par un test
- [ ] **IOSDI-02**: Les ViewModels reçoivent leurs dépendances par constructeur via des protocoles ; `BaseViewModel`, `RCValues.sharedInstance`, le global `remoteConfig`, `PerformanceMonitoring.shared` et `FirebaseAnalyticsService.shared` sont supprimés
- [ ] **IOSDI-03**: `scripts/swift-di-gate.sh`, exécuté en CI, interdit l'accès à `IosDependencies` et aux singletons hors de la composition root

### IOSMOD (Modularisation SPM)

- [ ] **IOSMOD-01**: `shared` est produit par Gradle en XCFramework statique et consommé en `binaryTarget` SPM ; l'app se build depuis un checkout propre en local et en CI, la phase `embedAndSignAppleFrameworkForXcode` est supprimée et l'archive Release conserve dSYMs et Crashlytics
- [ ] **IOSMOD-02**: Des packages/targets core locaux existent : pont Kotlin (seul importeur de `shared`), CoreServices (protocoles), FirebaseServices, DesignSystem (sortie SwiftGen et ressources via `Bundle.module`), CoreDI
- [ ] **IOSMOD-03**: Des packages/targets feature Agenda (détail de session inclus), Speakers, Venue et About existent, sans dépendance entre features ni import direct du framework Kotlin ; la target app est réduite à une coquille

### CONC (Concurrence Swift 6)

- [ ] **CONC-01**: Tous les packages et la target app compilent en mode de langage Swift 6 avec vérification complète de la concurrence et zéro warning
- [ ] **CONC-02**: Les ponts Kotlin (`asyncFunction`/`asyncSequence`, conformances `@unchecked Sendable` limitées aux types Kotlin immuables) sont confinés au package pont, contrôlés par un gate CI
- [ ] **CONC-03**: KMP-NativeCoroutines est à la même version (1.0.6) côté Swift (SPM) et Gradle, vérifié par un gate CI

### IOSTEST (Tests & CI iOS)

- [ ] **IOSTEST-01**: La target de test obsolète (`DevFest NantesTests`) est remplacée ; chaque ViewModel iOS a des tests Swift Testing sur des fakes
- [ ] **IOSTEST-02**: Chaque feature iOS a un smoke test SwiftUI (rendu via `UIHostingController`)
- [ ] **IOSTEST-03**: La CI iOS exécute `xcodebuild test` via un test plan incluant les tests des packages, échoue si aucun test n'est exécuté, et sa clé de cache inclut la version de Xcode
- [ ] **IOSTEST-04**: `swift-names-gate.sh` est restauré dans `scripts/` et exécuté en CI

### DOC (Documentation)

- [ ] **DOC-01**: `README.md`, `documentation/` (ARCHITECTURE, DEVELOPMENT, build dSYM iOS) et `AGENTS.md` décrivent la nouvelle architecture iOS et les commandes associées

## Future Requirements

Reportés, suivis mais hors roadmap v1.1.

- **FUT-01**: Unifier entre plateformes le créneau courant au scroll, les catégories de partenaires et la règle de fuseau des jours (changement visible)
- **FUT-02**: Dette v1.0 : `speakersPage` (110 speakers), `RoomSortIndex` pour `Tour Bretagne`/`Unconf`, `SocialItem`, page-view Speaker, état ERROR des ViewModels, durcissement CI WR-02/WR-03
- **FUT-03**: Montée Firebase iOS 12.x/13.x
- **FUT-04**: Exécution des tests sur un runtime iOS 17 en CI
- **FUT-05**: Tests snapshot / XCUITest
- **FUT-06**: Dates d'événement 2025 codées en dur remplacées par une source de données

## Out of Scope

| Feature | Reason |
|---------|--------|
| ViewModels partagés en KMP | Choix explicite : seule la couche domaine est partagée, ViewModels idiomatiques par plateforme |
| UI partagée (Compose Multiplatform) | UI native conservée sur Android (Compose) et iOS (SwiftUI) |
| SKIE / Swift Export | KMP-NativeCoroutines déjà en place et compatible ; Swift Export en Alpha et incompatible avec SPM ; SKIE en conflit avec `shared`/`.shared` |
| Bibliothèque DI Swift (Factory, swift-dependencies, Swinject) | Composition root manuelle suffisante, sans locator global |
| Nouvelles fonctionnalités utilisateur | Chantier purement technique, parité stricte |
| Correction des bugs découverts en cours de route | Parité stricte : consignés en dette, pas corrigés |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| SHARED-01 | Phase 7 | Pending |
| SHARED-02 | Phase 7 | Pending |
| SHARED-03 | Phase 7 | Pending |
| SHARED-04 | Phase 7 | Pending |
| SHARED-05 | Phase 7 | Pending |
| SHARED-06 | Phase 7 | Pending |
| SHARED-07 | Phase 7 | Pending |
| IOSDATA-01 | Phase 9 | Pending |
| IOSDATA-02 | Phase 9 | Pending |
| IOSDATA-03 | Phase 9 | Pending |
| IOSUI-01 | Phase 8 | Pending |
| IOSUI-02 | Phase 8 | Pending |
| IOSUI-03 | Phase 8 | Pending |
| IOSDI-01 | Phase 10 | Pending |
| IOSDI-02 | Phase 10 | Pending |
| IOSDI-03 | Phase 10 | Pending |
| IOSMOD-01 | Phase 11 | Pending |
| IOSMOD-02 | Phase 11 | Pending |
| IOSMOD-03 | Phase 11 | Pending |
| CONC-01 | Phase 12 | Pending |
| CONC-02 | Phase 12 | Pending |
| CONC-03 | Phase 6 | Pending |
| IOSTEST-01 | Phase 8 | Pending |
| IOSTEST-02 | Phase 12 | Pending |
| IOSTEST-03 | Phase 6 | Pending |
| IOSTEST-04 | Phase 6 | Pending |
| DOC-01 | Phase 12 | Pending |

**Coverage:**
- v1.1 requirements: 27 total
- Mapped to phases: 27
- Unmapped: 0 ✓

**Notes de traçabilité :**
- SHARED-05 est livré en Phase 7 (interfaces communes, implémentation Android inchangée, implémentation iOS sur les clés legacy testée sur simulateur) ; son câblage dans l'app iOS et l'UAT de mise à jour relèvent d'IOSDATA-01/02 (Phase 9).
- IOSTEST-01 est clos en Phase 8 (tests de chaque ViewModel) ; la target obsolète `DevFest NantesTests` est remplacée dès la Phase 6, prérequis de la lane IOSTEST-03.
- IOSTEST-03 est livré en Phase 6 ; la Phase 11 ajoute les tests des packages au test plan (critère de succès 4 de la Phase 11).
- IOSUI-01 couvre les targets existantes en Phase 8 ; les packages créés en Phase 11 naissent en `.iOS(.v17)` (critère de succès 3 de la Phase 11).

---
*Requirements defined: 2026-10-23*
*Last updated: 2026-10-23 after roadmap creation (phases 6-12)*
