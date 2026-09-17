# Requirements: DevFest Nantes App Modernization

**Defined:** 2026-09-12
**Core Value:** La CI/CD doit refonctionner et le projet doit redevenir maintenable (build moderne, architecture modulaire, DI décentralisée, couverture de tests solide) sans jamais régresser le comportement existant de l'application pour les utilisateurs.

## v1 Requirements

Requirements pour ce chantier de modernisation. Chaque requirement mappe vers une ou plusieurs phases de la roadmap.

### CI-IOS (CI iOS cassée)

- [x] **CI-01**: La CI iOS GitHub Actions se termine avec succès grâce à une résolution dynamique du simulateur (`xcrun simctl list` ou version macOS/Xcode explicitement épinglée), corrigeant la cause racine plutôt qu'un re-pin qui recassera à la prochaine rotation d'image runner

### BUILD (Dépendances & build system)

- [ ] **BUILD-01**: Le projet compile avec Kotlin 2.4.0
- [ ] **BUILD-02**: Le projet migre vers AGP 9.2.0 et le nouveau plugin `com.android.kotlin.multiplatform.library` (remplace la coexistence `kotlin.multiplatform` + `com.android.library` interdite en AGP 9+)
- [ ] **BUILD-03**: Le projet utilise Gradle 9.7.1
- [ ] **BUILD-04**: Compose BOM est mis à jour vers 2026.08.00
- [ ] **BUILD-05**: Apollo GraphQL est mis à jour vers 5.0.1
- [ ] **BUILD-06**: Firebase BOM, Kotlin Coroutines, kotlinx-serialization et kotlinx-datetime sont mis à jour vers leurs dernières versions stables
- [ ] **BUILD-07**: Les fichiers de build sont migrés vers le Gradle Declarative DSL (`.gradle.dcl`) là où le support AGP/KMP le permet ; les modules non supportés restent documentés en Kotlin DSL (`.kts`) avec la raison du blocage

### ARCH (Architecture multi-module)

- [ ] **ARCH-01**: Le build-logic du projet utilise des convention plugins + le version catalog existant (`libs.versions.toml`), évitant la duplication de configuration entre modules
- [ ] **ARCH-02**: Les modules `core-*` (model, network, data, analytics, ui, testing) existent et respectent le graphe de dépendances unidirectionnel (core ne dépend jamais de feature)
- [ ] **ARCH-03**: Les modules `feature-*` (agenda, speakers, venue, bookmarks, session-detail, settings) existent, chacun possédant ses propres ViewModel(s), écrans Compose et module Koin
- [ ] **ARCH-04**: `iosApp` continue de consommer un seul framework Kotlin/Native (framework umbrella agrégeant tous les modules KMP) malgré le découpage de `shared` en plusieurs modules Gradle

### DI (Migration Hilt → Koin)

- [ ] **DI-01**: La DI est entièrement migrée de Dagger Hilt vers Koin
- [ ] **DI-02**: Chaque module Gradle (`core-*`/`feature-*`) expose son propre module Koin (`module { }`), composé via un point d'entrée unique `initKoin()`
- [ ] **DI-03**: Les dépendances liées à la plateforme (Context Android, SharedPreferences, etc.) sont fournies via `expect`/`actual` ou des modules Koin par plateforme
- [ ] **DI-04**: Les ViewModels et Stores utilisent l'injection par constructeur — aucun appel `get()` dispersé en dehors de la couche de câblage DI
- [ ] **DI-05**: Un module `core-testing` centralise les fakes/doubles de test partagés (à la `DevFestNantesStoreMocked`) pour tous les modules feature
- [ ] **DI-06**: Un test `checkModules()` Koin vérifie le graphe de DI complet en CI

### TEST (Couverture de tests)

- [ ] **TEST-01**: Les fragilités connues bloquantes sont corrigées avant l'extension de la couverture (RNG non seedé dans `StoreStubs.kt`, thread-safety de `SimpleDateFormat` dans les chemins testés)
- [ ] **TEST-02**: La couverture `commonTest` de la logique métier (Store/repository, mappers GraphQL→model) est étendue en prolongeant le pattern `DevFestNantesStoreContractTest` existant vers les nouveaux modules `core-data`/feature
- [ ] **TEST-03**: La couverture de tests des ViewModels est étendue, rendue possible par l'injection par constructeur (Koin)
- [ ] **TEST-04**: Les tests de fumée Compose sur les écrans Android critiques sont maintenus/étendus par module feature (`androidTest`), en s'appuyant sur `core-testing`

### CICD (Optimisation CI/CD)

- [x] **CICD-01**: Le cache Gradle (`gradle/actions/setup-gradle`) est configuré en CI
- [x] **CICD-02**: Le cache Konan (`~/.konan`) est configuré en CI pour accélérer la compilation Kotlin/Native
- [x] **CICD-03**: Les jobs Android et iOS sont séparés dans une matrice CI (`ubuntu-latest`/`macos-latest`)

## v2 Requirements

Déférés à une future itération. Suivis mais pas dans la roadmap actuelle de ce milestone.

### ARCH

- **ARCH-V2-01**: Split `api`/`impl` par module feature pour les features à plus fort taux de changement (agenda, session-detail)

### CICD

- **CICD-V2-01**: Lint de dépendances entre modules en CI (empêche l'érosion du graphe de dépendances unidirectionnel après la migration)
- **CICD-V2-02**: Triggers CI filtrés par chemin (skip du job iOS sur diffs Android-only et inversement)

### DI

- **DI-V2-01**: Adoption de Koin Annotations/compiler plugin (`@Single`, `@KoinViewModel`, `@ComponentScan`) une fois le squelette multi-module stabilisé

## Out of Scope

Explicitement exclus. Documenté pour éviter le scope creep.

| Feature | Reason |
|---------|--------|
| Nouvelles fonctionnalités utilisateur / changements UX | Chantier purement technique — l'app doit se comporter exactement pareil après la modernisation |
| Taxonomie NIA complète (modules `sync`, `benchmark`, `lint`, `test-app`) | Solve des problèmes d'échelle qu'une app conférence unique n'a pas — scope creep |
| Modularisation par écran (un module par écran) | Sur-modularisation — le coût de configuration Gradle dépasserait le bénéfice pour ~6 features |
| Compose Multiplatform (UI partagée) pour iOS | iOS reste en SwiftUI natif — partager l'UI serait une réécriture UI avec risque de régression, hors périmètre |
| Objectif de couverture de tests en % chiffré | Rejeté explicitement — priorité qualitative à la logique métier plutôt qu'à un chiffre arbitraire |
| Mocking-framework-heavy testing (MockK/Mockative) par défaut | Le projet privilégie déjà les fakes (`DevFestNantesStoreMocked`) — pattern à étendre, pas à remplacer |
| Appels Koin `get()` en style service-locator dispersés dans le code métier | Recrée le couplage non testable que la migration DI est censée résoudre |
| Exécution du build+test iOS complet sur chaque commit de chaque branche | Coût CI élevé (runners macOS) sans bénéfice de sécurité proportionnel — géré plus tard via triggers filtrés (v2) |
| Tests de régression visuelle (Paparazzi/Roborazzi) | Non retenu par l'utilisateur pour ce milestone |
| Rester sur AGP 8.13.0 | Décision prise : migration vers AGP 9.2.0 + nouveau plugin KMP dès le début du chantier |
| Deadline calée sur la prochaine édition DevFest Nantes | Pas de contrainte de date stricte — avancement phase par phase |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| CI-01 | Phase 1 | Complete |
| BUILD-01 | Phase 2 | Pending |
| BUILD-02 | Phase 2 | Pending |
| BUILD-03 | Phase 2 | Pending |
| BUILD-04 | Phase 2 | Pending |
| BUILD-05 | Phase 2 | Pending |
| BUILD-06 | Phase 2 | Pending |
| BUILD-07 | Phase 2 | Pending |
| ARCH-01 | Phase 3 | Pending |
| ARCH-02 | Phase 3 | Pending |
| ARCH-03 | Phase 3 | Pending |
| ARCH-04 | Phase 3 | Pending |
| DI-01 | Phase 4 | Pending |
| DI-02 | Phase 4 | Pending |
| DI-03 | Phase 4 | Pending |
| DI-04 | Phase 4 | Pending |
| DI-05 | Phase 4 | Pending |
| DI-06 | Phase 4 | Pending |
| TEST-01 | Phase 5 | Pending |
| TEST-02 | Phase 5 | Pending |
| TEST-03 | Phase 5 | Pending |
| TEST-04 | Phase 5 | Pending |
| CICD-01 | Phase 1 | Complete |
| CICD-02 | Phase 1 | Complete |
| CICD-03 | Phase 1 | Complete |

**Coverage:**

- v1 requirements: 25 total
- Mapped to phases: 25 (5 phases)
- Unmapped: 0 ✓

---
*Requirements defined: 2026-09-12*
*Last updated: 2026-09-12 after roadmap creation (25/25 requirements mapped across 5 phases)*
