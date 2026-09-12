# DevFest Nantes App Modernization

## What This Is

L'application mobile DevFest Nantes (Kotlin Multiplatform : Android/Jetpack Compose + iOS/SwiftUI) permet aux participants de consulter l'agenda, les speakers, le lieu et de gérer leurs favoris pour l'événement DevFest Nantes. Le projet n'a pas évolué depuis un an et ce chantier vise une modernisation purement technique de sa base de code, sans changement de comportement utilisateur, pour repartir sur des bases saines avant de reprendre le développement de fonctionnalités.

## Core Value

La CI/CD doit refonctionner et le projet doit redevenir maintenable (build moderne, architecture modulaire, DI décentralisée, couverture de tests solide) sans jamais régresser le comportement existant de l'application pour les utilisateurs.

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

### Active

- [ ] CI iOS : corriger le pipeline GitHub Actions cassé (échec à l'étape "Build iOS App for Simulator" — simulateur "iPhone 16" introuvable)
- [ ] Build & dépendances : mise à jour complète de toutes les dépendances (Kotlin, AGP, Compose, Apollo, Hilt/Koin, Firebase, etc.)
- [ ] Build & dépendances : migration des fichiers de build vers le nouveau Gradle Declarative DSL (`.gradle.dcl`) — migration complète si le support AGP/KMP le permet, sinon migration partielle documentée module par module avec fallback en Kotlin DSL (`.kts`) là où le support manque encore
- [ ] Architecture : découpage du projet en architecture multi-modules (modules `core-*` : data, network, ui, analytics, testing + modules `feature-*` : agenda, speakers, venue, bookmarks, session-detail, settings), inspirée de Now in Android et adaptée au contexte KMP (Android + iOS)
- [ ] DI : migration complète de Hilt vers Koin (fonctionne nativement en KMP, Android et iOS), avec un module Koin déclaré par module Gradle (DI décentralisée)
- [ ] Qualité : amélioration significative de la couverture de tests unitaires et UI, priorité sur la logique métier (ViewModels, Store/repository, mappers GraphQL→model) — pas d'objectif % strict imposé
- [ ] DevOps : mise à niveau de la CI/CD GitHub Actions pour optimiser les temps de build KMP (cache Gradle/Kotlin, parallélisation des modules, builds incrémentaux)

### Out of Scope

- Nouvelles fonctionnalités utilisateur — chantier purement technique, l'app doit se comporter exactement pareil après la modernisation
- Changements UX/UI visibles — sauf effets de bord inévitables liés à la migration technique
- Deadline calée sur la prochaine édition de DevFest Nantes — pas de contrainte de date stricte, avancement phase par phase

## Context

- Le projet est un monorepo KMP existant : module `shared` (commonMain, Kotlin) partagé entre `androidApp` (Jetpack Compose, Hilt) et `iosApp` (SwiftUI, via Cocoapods).
- Stack actuelle (voir `.planning/codebase/STACK.md`) : Kotlin 2.2.0, AGP 8.13.0, Gradle 8.13.0 (Kotlin DSL `.kts`), Jetpack Compose BOM 2025.09.01, Apollo GraphQL 4.3.3, Dagger Hilt 2.57.2, Navigation Compose 2.9.5, Coroutines 1.10.2, Firebase BOM 33.16.0.
- Architecture actuelle (voir `.planning/codebase/ARCHITECTURE.md`) : Clean Architecture en couches (Presentation → Application/Service → Data/Store → Domain/Model), pas de découpage multi-module — tout est dans `androidApp` et `shared`.
- Le module `shared` ne dépend pas de `androidApp` (pas d'imports circulaires) — bonne base pour la modularisation.
- DI actuelle : Dagger Hilt, un seul `AppModule` au niveau `SingletonComponent` dans `androidApp` — Hilt n'est pas utilisable dans `shared` (KMP), ce qui bloque une vraie DI décentralisée.
- Anti-patterns connus à corriger si rencontrés pendant la modernisation (voir `.planning/codebase/ARCHITECTURE.md`) : `println()` pour le logging d'erreurs GraphQL au lieu de Timber, références directes à Firebase dans certains ViewModels, absence de gestion d'erreur dans les blocs `init {}` des ViewModels.
- Un mapping de codebase existe déjà dans `.planning/codebase/` (ARCHITECTURE.md, STACK.md, STRUCTURE.md, CONVENTIONS.md, INTEGRATIONS.md, TESTING.md, CONCERNS.md).

## Constraints

- **Outillage** : L'agent DOIT utiliser le CLI `android` pour toute tâche Android — en particulier `android docs search` pour vérifier la syntaxe exacte du Gradle Declarative DSL et d'autres APIs récentes, et les commandes de lint/validation avant de considérer une modification terminée.
- **Compatibilité fonctionnelle** : Aucune régression de comportement utilisateur n'est acceptable — chaque étape de la modernisation doit préserver le comportement existant de l'app (Android et iOS).
- **KMP** : Toute solution technique (DI, modularisation) doit fonctionner à la fois sur Android et iOS via le module `shared`.
- **Gradle Declarative DSL** : Technologie en preview/incubation — le support AGP/KMP peut être partiel selon les versions. Accepter une migration partielle documentée plutôt que de bloquer sur une conversion à 100%.
- **CI/CD** : GitHub Actions reste la plateforme CI/CD cible (pas de changement d'outil CI).

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Ordre des chantiers : CI iOS d'abord, puis deps/Gradle DSL, puis multi-module, puis DI, puis tests | Le CI cassé est bloquant et un quick win ; les fondations de build doivent être stables avant de refactorer l'architecture | — Pending |
| DI : migration complète Hilt → Koin | Hilt ne fonctionne pas dans le module `shared` KMP ; Koin est natif KMP et permet une vraie DI décentralisée par module | — Pending |
| Découpage multi-module : feature + core (façon Now in Android complet) | Vision NIA complète demandée explicitement par l'utilisateur, adaptée au contexte KMP (Android + iOS) | — Pending |
| Gradle Declarative DSL : migration complète si possible, sinon partielle documentée | La techno est en incubation ; éviter de bloquer tout le projet sur un support incomplet côté AGP/KMP | — Pending |
| Couverture de tests : pas d'objectif % strict, priorité à la logique métier | L'utilisateur préfère une couverture qualitative (ViewModels, Store, mappers) à un chiffre arbitraire | — Pending |
| Aucune nouvelle fonctionnalité utilisateur pendant ce chantier | Chantier purement technique — réduire le risque de régression en isolant modernisation et évolution fonctionnelle | — Pending |
| Pas de deadline stricte liée à la prochaine édition | Chantier de fond mené phase par phase sans pression de date | — Pending |

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
*Last updated: 2026-09-12 after initialization*
