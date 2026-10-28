#!/usr/bin/env bash
# Re-records the agenda characterization snapshot (Phase 7, D-09).
#
# Step 1 posts the app's three agenda queries to the live confetti-app.dev API and writes the raw
# responses to core/data/testdata/graphql/ (AgendaSnapshotRecorder). Step 2 rewrites the neutral
# snapshot in core/domain/testdata/agenda/ from those raw files (AgendaSnapshotTest, record mode).
# They are two Gradle invocations so the order is guaranteed. Tests stay offline in a normal run:
# both are excluded from jvmTest unless -PrecordAgendaSnapshot is passed.
#
# Usage: bash scripts/record-agenda-snapshot.sh
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

./gradlew :core:data:jvmTest -PrecordAgendaSnapshot --tests '*AgendaSnapshotRecorder*'

# AgendaSnapshotTest is added by a later plan. Until it exists Gradle reports "No tests found",
# which is expected and tolerated; any other failure is real and stops the script.
output=$(mktemp)
trap 'rm -f "$output"' EXIT
if ! ./gradlew :core:data:jvmTest -PrecordAgendaSnapshot --tests '*AgendaSnapshotTest*' 2>&1 | tee "$output"; then
    if grep -q 'No tests found' "$output"; then
        echo "No AgendaSnapshotTest yet: the neutral snapshot was not rewritten." >&2
    else
        exit 1
    fi
fi

echo "AGENDA-SNAPSHOT-RECORDED"
