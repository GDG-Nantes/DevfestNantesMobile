# DevFest Nantes App Modernization

## What This Is

L'application mobile DevFest Nantes (Kotlin Multiplatform : Android/Jetpack Compose + iOS/SwiftUI) permet aux participants de consulter l'agenda, les speakers, le lieu et de gérer leurs favoris pour l'événement DevFest Nantes. Le projet n'a pas évolué depuis un an et ce chantier vise une modernisation purement technique de sa base de code, sans changement de comportement utilisateur, pour repartir sur des bases saines avant de reprendre le développement de fonctionnalités.

## Core Value

La CI/CD doit refonctionner et le projet doit redevenir maintenable (build moderne, architecture modulaire, DI décentralisée, couverture de tests solide) sans jamais régresser le comportement existant de l'application pour les utilisateurs.

## Current State

**v1.0 Modernization livrée le 2026-10-16** (phases 1-5, 37 plans, PR #417 à #421). CI Android/iOS verte et mise en cache, toolchain à jour (Kotlin 2.4.20, AGP 9.4.0, Gradle 9.7.1), architecture `build-logic` + 6 `core-*` + 6 `feature-*` avec `:shared` en framework iOS parapluie, DI Koin 4.2.2 vérifiée en CI, couverture Kover 30,57 %. Aucun changement de comportement utilisateur. Audit : `milestones/v1.0-MILESTONE-AUDIT.md` (tech_debt, 25/25 exigences).

## Current Milestone: v1.1 iOS Architecture

**Goal:** Moderniser l'app iOS au même niveau que l'app Android — logique métier partagée en KMP, app SwiftUI modulaire en Observation + Swift 6 strict, couverture de tests réelle — sans aucun changement de comportement utilisateur.

**Target features:**
- Couche domaine KMP partagée : regroupement/tri de l'agenda, filtrage des sessions, favoris, mapping speakers/venue/partenaires exposés comme use cases/repositories consommés par Android et iOS (ViewModels restant natifs sur chaque plateforme)
- Suppression de la logique dupliquée côté iOS : favoris et filtres de l'agenda sortent d'`UserDefaults` dans `AgendaViewModel` vers la logique partagée, avec migration des données existantes (aucun favori perdu)
- App iOS modulaire : packages SPM locaux par feature (Agenda, Speakers, Venue, About…) + packages core (DesignSystem, Core/DI), target app réduite à une coquille
- DI iOS explicite : composition root injectant les dépendances dans les ViewModels au lieu des lookups `IosDependencies.shared` et des singletons globaux (`RCValues`, `PerformanceMonitoring`, global `remoteConfig`)
- SwiftUI moderne : iOS 17 minimum sur toutes les targets, ViewModels `@Observable` à la place d'`ObservableObject`/`@Published`, Combine supprimé
- Swift 6 strict : mode de langage Swift 6 avec vérification complète de la concurrence, ViewModels `@MainActor`, async/await sur les Flows Kotlin
- Tests & CI : tests commonTest des use cases partagés (JVM + simulateur iOS), tests Swift Testing de chaque ViewModel iOS, smoke test SwiftUI par feature, tests iOS exécutés dans GitHub Actions

## Requirements

### Validated

- ✓ Consultation de l'agenda des sessions (Agenda tab) — existing
- ✓ Détail d'une session (SessionLayout/SessionViewModel) — existing
- ✓ Consultation des speakers (Speaker screens) — existing
- ✓ Consultation du lieu/venue (Venue screen) — existing
- ✓ Gestion des favoris/bookmarks (BookmarksStore, persistés en SharedPreferences) — existing
- ✓ Filtrage des sessions (SessionFiltersService) — existing
- ✓ Récupération des données via GraphQL (Apollo Client, endpoint confetti-app.dev) — existing
- ✓ Analytics (Firebase Analytics/Crashlytics/Performance) — existing
- ✓ Intégration OpenFeedback conditionnelle — existing
- ✓ Cible Android (API 23-36, Jetpack Compose) et iOS (SwiftUI, via Cocoapods) — existing
- ✓ CI iOS : pipeline GitHub Actions réparé (résolution dynamique du simulateur "latest plain iPhone" + Xcode le plus récent installé, au lieu du nom "iPhone 16" en dur) — v1.0 (Phase 1)
- ✓ DevOps : CI/CD GitHub Actions optimisée (cache Gradle en lecture seule sur les runs `pull_request` pour les deux workflows, action composite `android-setup` partagée par Android et iOS, preuve de run réel en `pull_request` sur les quatre jobs Android) — v1.0 (Phase 1)
- ✓ Build & dépendances : mise à jour complète des dépendances (Kotlin 2.4.20, AGP 9.4.0, Gradle 9.7.1, Compose BOM 2026.09.00, Apollo 5.2.0, Firebase BOM 34.19.0…) — v1.0 (Phase 2)
- ✓ Build & dépendances : Gradle Declarative DSL en migration partielle documentée (`settings.gradle.dcl` ; modules restés en `.kts` faute de support AGP/KMP) — v1.0 (Phase 2)
- ✓ Architecture : découpage multi-modules façon Now in Android adapté KMP — `build-logic` (6 convention plugins, `buildSrc` supprimé), `:core:{model,network,analytics,data,testing,ui}`, `:feature:{venue,about,settings,speakers,agenda,session-detail}` (entrées `*Route` par callbacks), `:shared` réduit à un framework iOS parapluie unique (bookmarks sans écran, dans `core:data`/`core:ui`) — v1.0 (Phase 3)
- ✓ DI : migration complète de Hilt vers Koin 4.2.2 — un module Koin par module Gradle, composé par `initKoin()` dans `:shared` (`allowOverride(false)`), `IosDependencies` côté iOS sans nom Koin dans `shared.h`, graphe vérifié en CI (`verify()` + tests de démarrage JVM/iOS/Robolectric), `scripts/di-gate.sh` strict contre Hilt et les appels service-locator — v1.0 (Phase 4)
- ✓ Qualité : couverture de lignes Kover passée de 15,96 % à 30,57 % — tests GraphQLStore/mappers sur Apollo MockServer (JVM/Android/iOS), contrat de Store partagé, persistance SharedPreferences épinglée, tests unitaires des dix ViewModels sur un fake contrôlable, matrice de filtres de l'agenda, smoke tests Compose des six modules feature ; crash OpenFeedback/kotlinx-datetime (G-05-3) corrigé et vérifié sur device — v1.0 (Phase 5)

### Active

Milestone v1.1 iOS Architecture (détail et REQ-IDs dans `REQUIREMENTS.md`) :
- [ ] Logique domaine (agenda, filtres, favoris, mapping) partagée en KMP et consommée par Android et iOS
- [ ] Plus aucune logique métier dupliquée côté iOS ; données `UserDefaults` existantes migrées sans perte
- [ ] App iOS découpée en packages SPM locaux feature/core, target app en coquille
- [ ] DI iOS explicite par composition root, sans service-locator ni singletons globaux dans les ViewModels
- [ ] iOS 17 minimum, ViewModels `@Observable`, Combine supprimé
- [ ] Swift 6 strict concurrency sans warning
- [ ] Tests partagés + Swift Testing + smoke tests SwiftUI, exécutés en CI iOS

### Out of Scope

- Nouvelles fonctionnalités utilisateur — v1.1 reste un chantier purement technique (comme v1.0)
- Dette v1.0 (`speakersPage`, `RoomSortIndex`, `SocialItem`, page-view Speaker, état ERROR des ViewModels, durcissement CI WR-02/WR-03) — reportée ; les bugs découverts pendant v1.1 sont consignés en dette, pas corrigés (parité stricte)
- ViewModels partagés en KMP (androidx.lifecycle KMP / SKIE) — choix de ne partager que la couche domaine ; chaque plateforme garde des ViewModels idiomatiques
- UI partagée (Compose Multiplatform) — UI native conservée sur Android (Compose) et iOS (SwiftUI)
- Changements UX/UI visibles — sauf effets de bord inévitables liés à la migration technique
- Deadline calée sur la prochaine édition de DevFest Nantes — pas de contrainte de date stricte, avancement phase par phase

## Context

- État après v1.0 : monorepo KMP `build-logic` (convention plugins `devfest.*`) + `:core:{model,network,analytics,data,testing,ui}` + `:feature:{agenda,speakers,venue,session-detail,about,settings}` + `:androidApp` + `:shared` (framework iOS parapluie exportant core:model/data/analytics) + `iosApp` (SwiftUI).
- Stack : Kotlin 2.4.20, AGP 9.4.0, Gradle 9.7.1 (`settings.gradle.dcl`, modules en `.kts`), Compose BOM 2026.09.00, Apollo 5.2.0, Koin 4.2.2, Firebase BOM 34.19.0, Coroutines 1.11.0, kotlinx-datetime `0.8.0-0.6.x-compat`, minSdk 26 / compileSdk 37.
- Taille : ~15,8k lignes Kotlin, ~3,8k lignes Swift ; v1.0 a touché 328 fichiers (+9 795 / −2 005).
- Tests : Kover 30,57 % (rapport seul), tests JVM/Android host/iOS simulator + smoke tests Compose par module feature, `verify()` Koin et `scripts/di-gate.sh` en CI.
- Dette connue : voir Active ci-dessus et STATE.md § Deferred Items. OpenFeedback dépend encore de `kotlinx.datetime.Instant` (pin compat limité à 0.8.x : correctif durable = release OpenFeedback sur kotlin.time).
- Les documents `.planning/codebase/` ont été régénérés après v1.0 (commit `99edc0a`).
- État iOS au démarrage de v1.1 : ~45 fichiers Swift dans une seule target app (pas de package local), ViewModels `ObservableObject` + Combine dérivant de `BaseViewModel` qui résout `IosDependencies.shared.store()`, favoris (`"Favorites"`) et filtres (`"AgendaSessionFilters"`) persistés en `UserDefaults` par `AgendaViewModel`, singletons globaux (`RCValues`, `PerformanceMonitoring`, `remoteConfig`), deployment targets mixtes 15.0/17.0, `SWIFT_VERSION = 5.0`, SPM (Firebase, KMP-NativeCoroutines), une seule classe de test (`DevFest NantesTests`) non exécutée en CI.

## Constraints

- **Outillage** : L'agent DOIT utiliser le CLI `android` pour toute tâche Android — en particulier `android docs search` pour vérifier la syntaxe exacte du Gradle Declarative DSL et d'autres APIs récentes, et les commandes de lint/validation avant de considérer une modification terminée.
- **Compatibilité fonctionnelle** : Aucune régression de comportement utilisateur n'est acceptable — chaque étape de la modernisation doit préserver le comportement existant de l'app (Android et iOS).
- **KMP** : Toute solution technique (DI, modularisation) doit fonctionner à la fois sur Android et iOS via le module `shared`.
- **Gradle Declarative DSL** : Technologie en preview/incubation — le support AGP/KMP peut être partiel selon les versions. Accepter une migration partielle documentée plutôt que de bloquer sur une conversion à 100%.
- **CI/CD** : GitHub Actions reste la plateforme CI/CD cible (pas de changement d'outil CI).

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Ordre des chantiers : CI iOS d'abord, puis deps/Gradle DSL, puis multi-module, puis DI, puis tests | Le CI cassé est bloquant et un quick win ; les fondations de build doivent être stables avant de refactorer l'architecture | ✓ Phase 1 shipped — CI iOS réparé + cache Gradle read-only en PR sur les deux workflows |
| Cache Gradle : expression `cache-read-only` dupliquée à chaque site d'appel plutôt que déplacée dans l'input par défaut de l'action composite `android-setup` | Les métadonnées d'une composite action sont parsées statiquement ; une expression dans l'input default serait transmise à `setup-gradle` en texte brut non interpolé et coercée en chaîne toujours vraie | Phase 1 |
| `cancel-in-progress: true` conservé tel quel sur `ios.yml`/`android.yml` (pas de `cancel-in-progress` conditionnel par event, pas de `queue`) | Comportement de coût voulu (D-07), auto-cicatrisant au push suivant ; `queue` + `cancel-in-progress` est une erreur de validation GitHub bloquante | Phase 1 — accepté comme risque documenté (T-01-05) |
| DI : migration complète Hilt → Koin | Hilt ne fonctionne pas dans le module `shared` KMP ; Koin est natif KMP et permet une vraie DI décentralisée par module | ✓ Phase 4 shipped — aucun artefact Hilt sur le classpath, DI-01..06 complets, UAT Android (debug + release R8) et iOS approuvée, 27/27 menaces sécurité closes |
| Découpage multi-module : feature + core (façon Now in Android complet) | Vision NIA complète demandée explicitement par l'utilisateur, adaptée au contexte KMP (Android + iOS) | ✓ Phase 3 shipped — 12 modules + `build-logic`, graphe strictement core ← feature ← app, CI verte, 5 smoke tests iOS/Android approuvés, 34/34 menaces sécurité closes |
| Phase 3 : `:shared` conserve un unique fichier interne `SharedFrameworkPlaceholder.kt` (amendement D-10) | Kotlin/Native saute compile+link (NO-SOURCE) sur un source set vide → aucun `shared.framework` produit | Phase 3 — approuvé par l'utilisateur au checkpoint 03-03 ; ne jamais supprimer |
| Phase 3 : types Apollo renommés via `@targetName` (GraphQLVenue/Session/Speaker/Room/Partner), noms Swift des 5 types domaine changés une fois (amendement D-11) | Supprime définitivement la collision de noms Swift Kotlin/Native qui a stoppé 03-01 ; `swift-names-gate.sh` renforcé (member sets + collisions) | Phase 3 (03-10/03-11) |
| Phase 3 : heap du daemon Gradle porté à 4 Go | OOM de D8 (dex merging) dans le job CI Instrumentation tests avec 6 modules feature | Phase 3 — approuvé par l'utilisateur |
| Gradle Declarative DSL : migration complète si possible, sinon partielle documentée | La techno est en incubation ; éviter de bloquer tout le projet sur un support incomplet côté AGP/KMP | ✓ v1.0 — partielle documentée (`settings.gradle.dcl` seulement) |
| Couverture de tests : pas d'objectif % strict, priorité à la logique métier | L'utilisateur préfère une couverture qualitative (ViewModels, Store, mappers) à un chiffre arbitraire | ✓ Phase 5 shipped — Kover 0.9.11 en rapport seul, 15,96 % → 30,57 %, UAT 3/3, 0 menace ouverte |
| Aucune nouvelle fonctionnalité utilisateur pendant ce chantier | Chantier purement technique — réduire le risque de régression en isolant modernisation et évolution fonctionnelle | ✓ v1.0 — tenu, UAT sans régression sur les 5 phases |
| Pas de deadline stricte liée à la prochaine édition | Chantier de fond mené phase par phase sans pression de date | ✓ v1.0 — livré en ~30 jours |
| Phase 4 : ordre des `ApplicationInitializer` explicite (`single(named("applicationInitializers"))`), asserté par `KoinStartupTest` | Le consentement de collecte doit être appliqué avant que Performance Monitoring soit forcé (T-04-19) | Phase 4 |
| Phase 4 : Robolectric 4.17 en `testImplementation` pour tester le démarrage réel du graphe Koin avec Firebase | Exerce les définitions Firebase de production sans les modifier ; base réutilisable pour la Phase 5 | Phase 4 — choisi par l'utilisateur |
| Phase 5 : kotlinx-datetime résolu en `0.8.0-0.6.x-compat` (pas de retour à 0.6.2), code first-party sur `kotlin.time.Instant`, suppressions R8 retirées | OpenFeedback 1.0.0-alpha.3 (et alpha.6) référence encore `kotlinx.datetime.Instant` ; la variante compat restaure ces classes tout en gardant la 0.8. Les releases compat s'arrêtent à 0.8.x : le correctif durable est une release OpenFeedback sur kotlin.time | Phase 5 (05-11) — gardé en CI par `OpenFeedbackDatetimeLinkageTest` |
| Phase 5 : seam `FeedbackFlags` au-dessus de Firebase Remote Config, IO dispatcher d'`AgendaViewModel` injecté par binding Koin nommé | Rendre FeedbackForm et Agenda testables sur JVM pur sans changer le comportement | Phase 5 (05-06, 05-08) |
| Phase 5 : les échecs de store non catchés dans les ViewModels sont épinglés par des tests de caractérisation, pas corrigés | Chantier sans changement de comportement ; correction (état ERROR) reportée — voir `deferred-items.md` et `05-UI-REVIEW.md` | Phase 5 — dette connue |
| Phase 2 : montée de version en groupes séquentiels (un commit par groupe), avec un CI vert entre chaque étape | Des montées majeures cumulées seraient impossibles à diagnostiquer en cas d'échec ; chaque groupe obtient son propre commit revertible et son propre signal vert avant d'attaquer le suivant | ✓ Phase 2 shipped — Kotlin 2.4.20, AGP 9.4.0 + plugin `com.android.kotlin.multiplatform.library`, Gradle 9.7.1, Compose BOM 2026.09.00, Apollo 5.2.0 (cache migré vers `com.apollographql.cache`), Firebase BOM 34.19.0/Coroutines 1.11.0/kotlinx-serialization 1.11.0/kotlinx-datetime 0.8.0 ; pilote Gradle Declarative DSL réussi sur `settings.gradle.dcl` (androidApp/shared restent en Kotlin DSL, support AGP/KMP DCL non mûr pour ces modules) — détail complet dans `.planning/STATE.md` § Phase 02 version deviations / DCL pilot outcome |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-10-23 after starting milestone v1.1 iOS Architecture*
