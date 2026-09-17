---
schema_version: 1
open_count: 1
waived_count: 0
fixed_count: 0
total_count: 1
last_updated: 2026-09-17T13:32:29.333Z
---

# Broken Windows Ledger

> Cross-phase defect register. With `workflow.windows_enforce` enabled, `/gsd-ship` blocks while `open_count > 0`.
> Waive with `gsd-tools windows waive <id> "<reason>"` (reason required).
> Mark fixed with `gsd-tools windows fixed <id>`.

| id | phase | kind | file | line | description | status | reason | recorded_at | resolved_at |
|----|-------|------|------|------|-------------|--------|--------|-------------|-------------|
| 1 | 01 | unrun-verify | .github/workflows/android.yml |  | Task 2 human-check deferred: Android CI green + read-only cache on phase PR to main not yet observed (no PR exists at plan execution time; android.yml has no workflow_dispatch to trigger directly) | open |  | 2026-09-17T13:32:29.333Z |  |

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
  }
]
````
