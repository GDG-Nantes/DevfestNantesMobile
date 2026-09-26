---
schema_version: 1
open_count: 2
waived_count: 0
fixed_count: 0
total_count: 2
last_updated: 2026-09-24T18:07:41.861Z
---

# Broken Windows Ledger

> Cross-phase defect register. With `workflow.windows_enforce` enabled, `/gsd-ship` blocks while `open_count > 0`.
> Waive with `gsd-tools windows waive <id> "<reason>"` (reason required).
> Mark fixed with `gsd-tools windows fixed <id>`.

| id | phase | kind | file | line | description | status | reason | recorded_at | resolved_at |
|----|-------|------|------|------|-------------|--------|--------|-------------|-------------|
| 1 | 01 | unrun-verify | .github/workflows/android.yml |  | Task 2 human-check deferred: Android CI green + read-only cache on phase PR to main not yet observed (no PR exists at plan execution time; android.yml has no workflow_dispatch to trigger directly) | open |  | 2026-09-17T13:32:29.333Z |  |
| 2 | 03 | deviation | androidApp/build.gradle.kts |  | 03-04: merged release AndroidManifest's androidx.compose.ui.tooling.PreviewActivity element repositioned (manifest-merger dependency-graph order) after moving compose deps into build-logic conventions; verified semantically identical via canonicalized XML comparison of all activity/service/receiver/provider/permission elements (same count, same attributes) -- not a security or behavior regression | open |  | 2026-09-24T18:07:41.861Z |  |

````json
[
  {
    "id": 1,
    "kind": "unrun-verify",
    "phase": "01",
    "file": ".github/workflows/android.yml",
    "line": null,
    "description": "Task 2 human-check deferred: Android CI green + read-only cache on phase PR to main not yet observed (no PR exists at plan execution time; android.yml has no workflow_dispatch to trigger directly)",
    "status": "open",
    "reason": "",
    "recorded_at": "2026-09-17T13:32:29.333Z",
    "resolved_at": null,
    "milestone": null
  },
  {
    "id": 2,
    "kind": "deviation",
    "phase": "03",
    "file": "androidApp/build.gradle.kts",
    "line": null,
    "description": "03-04: merged release AndroidManifest's androidx.compose.ui.tooling.PreviewActivity element repositioned (manifest-merger dependency-graph order) after moving compose deps into build-logic conventions; verified semantically identical via canonicalized XML comparison of all activity/service/receiver/provider/permission elements (same count, same attributes) -- not a security or behavior regression",
    "status": "open",
    "reason": "",
    "recorded_at": "2026-09-24T18:07:41.861Z",
    "resolved_at": null,
    "milestone": null
  }
]
````
