#!/usr/bin/env bash
# Re-records the agenda characterization snapshot (Phase 7, D-09).
#
# Step 1 posts the app's three agenda queries to the live confetti-app.dev API and writes the raw
# responses to core/data/testdata/graphql/ (AgendaSnapshotRecorder). Step 2 rewrites the neutral
# snapshot in core/domain/testdata/agenda/ from those raw files (AgendaSnapshotTest, record mode,
# only the rewrite test).
# They are two Gradle invocations so the order is guaranteed. Tests stay offline in a normal run:
# both are excluded from jvmTest unless -PrecordAgendaSnapshot is passed.
#
# Usage: bash scripts/record-agenda-snapshot.sh
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

./gradlew :core:data:jvmTest -PrecordAgendaSnapshot --tests '*AgendaSnapshotRecorder*'

# Only the rewrite test runs here, so the tests that pin the counts of the previous recording
# (recorded_snapshot_has_the_expected_shape) cannot block re-recording. Any failure, including
# "No tests found" after a rename, stops the script: set -e exits on a non-zero Gradle status.
./gradlew :core:data:jvmTest -PrecordAgendaSnapshot \
    --tests '*AgendaSnapshotTest.neutral_snapshot_matches_the_raw_responses'

echo "Review the diff, then update the counts pinned in AgendaSnapshotTest (recorded_snapshot_has_the_expected_shape)." >&2
echo "AGENDA-SNAPSHOT-RECORDED"
