# Phase 6: Socle de tests et gates CI iOS - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-10-26
**Phase:** 6-Socle de tests et gates CI iOS
**Areas discussed:** Premier test réel, Structure du job CI, Restauration des gates, Validation du bump KMP-NC

---

## Premier test réel

### Contenu des premiers tests

| Option | Description | Selected |
|--------|-------------|----------|
| Logique Swift pure | Tests de types Swift sans réseau ni Koin (`SessionFilter` Codable, `getDate`) | ✓ |
| Un seul test canari | Test trivial prouvant que la lane tourne | |
| Caractérisation agenda | Figer le regroupement/filtrage Swift comme oracle de la Phase 9 | |

### Hébergement de la target

| Option | Description | Selected |
|--------|-------------|----------|
| Hébergée, init neutralisé | `TEST_HOST` conservé ; `iOSApp.init` saute Firebase, Koin et Remote Config sous XCTest | ✓ |
| Hébergée, init inchangé | Chaque run démarre Firebase (plist factice), Koin, fetch Remote Config | |
| Non hébergée | Bundle autonome compilant ses propres sources, pas de `@testable import` | |

### Ancienne target `DevFest NantesTests`

| Option | Description | Selected |
|--------|-------------|----------|
| Supprimer, nouvelle target | Supprimer target + 2 fichiers, créer une target au nom sans espace | ✓ |
| Garder la target, vider | Garder la target et remplacer son contenu | |

**User's choice:** options recommandées sur les trois questions.

---

## Structure du job CI

### Découpage

| Option | Description | Selected |
|--------|-------------|----------|
| Un seul job | `build-for-testing` + `test-without-building` dans le job existant | ✓ |
| Deux jobs | Gradle/framework puis Swift build/test via artefact | |
| Trois jobs gates/build/test | Gates isolés en plus du découpage | |

### Seuil `.xcresult`

| Option | Description | Selected |
|--------|-------------|----------|
| Échec si 0 test | Critère de la roadmap, rien à maintenir | ✓ |
| Minimum configurable | `MIN_TESTS` relevé à chaque phase | |

### Robustesse (multi-sélection)

| Option | Description | Selected |
|--------|-------------|----------|
| Artefact `.xcresult` en échec | Téléversement pour diagnostic | ✓ |
| Simulateur par UDID + boot | `simctl boot` + `bootstatus` | ✓ |
| Parallélisme désactivé | `-parallel-testing-enabled NO`, aucun retry | ✓ |
| Résumé du job | Xcode, simulateur, nombre de tests dans `$GITHUB_STEP_SUMMARY` | ✓ |

---

## Restauration des gates

### Emplacement

| Option | Description | Selected |
|--------|-------------|----------|
| `scripts/` + `scripts/swift-names/` | Script à côté de `di-gate.sh`, baselines sans préfixe `03-` | ✓ |
| Tout à plat dans `scripts/` | Script et 4 fichiers `.txt` dans `scripts/` | |
| Baselines sous `iosApp/` | Baselines près du code Swift | |

### Selftest en CI

| Option | Description | Selected |
|--------|-------------|----------|
| Check + selftest | Prouve que le gate détecte toujours swap et collision | ✓ |
| Check seulement | Selftest manuel | |

### Gate KMP-NC

| Option | Description | Selected |
|--------|-------------|----------|
| Script dédié | `scripts/kmp-nc-version-gate.sh`, rapide, exécutable en local | ✓ |
| Intégré à swift-names-gate | Un seul gate, deux responsabilités | |
| Étape inline dans `ios.yml` | Pas exécutable en local | |

### `resources-gate.sh`

Premier choix : « Oui, dans cette phase ». Précision apportée ensuite : le script est une preuve de
migration de ressources **Android** one-shot (comparaison avec `origin/main` de `androidApp/res`),
sans objet tel quel.

| Option | Description | Selected |
|--------|-------------|----------|
| Reporter | Idée reportée ; gate ressources iOS plutôt en Phase 11 | ✓ |
| Réécrire en invariant Android | Vérification permanente branchée dans `android.yml` | |
| Gate de ressources iOS | Clés `en.lproj`/`fr.lproj` identiques dans `ios.yml` | |

**User's choice:** Reporter.

---

## Validation du bump KMP-NC

### Preuve de parité

| Option | Description | Selected |
|--------|-------------|----------|
| Smoke manuel guidé | Checkpoint humain sur simulateur, captures (pattern UAT v1.0) | ✓ |
| Smoke manuel + vérif auto | Plus un test Swift de compilation de l'API KMP-NC | |
| CI verte suffit | Aucune vérification runtime des flows | |

### Ordre

| Option | Description | Selected |
|--------|-------------|----------|
| Lane de tests d'abord | Target + lane, puis gates, puis bump ; gate KMP-NC dans le commit du bump | ✓ |
| Bump d'abord | Aligner la version en premier | |
| Au choix du planner | | |

---

## Claude's Discretion

- Mécanisme de détection « sous XCTest » dans `iOSApp.init`
- Deployment target et `SWIFT_VERSION` de la nouvelle target
- Cache SPM séparé et composition des clés de cache au-delà de la version Xcode
- Noms du `.xctestplan` / de la target, emplacement du parsing `xcresulttool`
- Mises à jour de documentation associées

## Deferred Ideas

- `resources-gate.sh` → gate ressources iOS en Phase 11 ou invariant Android dédié
- Seuil minimum de tests configurable
- Découpage du job iOS si le timeout de 30 min est dépassé
