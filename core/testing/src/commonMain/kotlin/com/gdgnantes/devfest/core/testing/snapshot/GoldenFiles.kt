package com.gdgnantes.devfest.core.testing.snapshot

import kotlin.test.fail

/**
 * Root of `core/domain/testdata` on this runtime: system property `devfest.domainTestdata`
 * (JVM and Android) or environment variable `DOMAIN_TESTDATA` (iOS simulator).
 */
internal expect fun testDataRoot(): String?

/** Reads a UTF-8 text file, or returns null when it does not exist. */
internal expect fun readFileOrNull(path: String): String?

/** Writes a UTF-8 text file, creating the parent directories. */
internal expect fun writeFile(path: String, content: String)

/** True only in explicit record mode (`-PrecordGoldens`, `DOMAIN_RECORD_GOLDENS=true` on iOS). */
internal expect fun recordGoldens(): Boolean

/**
 * Access to the shared test data and golden files under `core/domain/testdata`.
 *
 * Golden rules (see the README next to the data): a default run never writes; an absent golden
 * fails with `golden missing: <path>`; in record mode an absent golden is written and the test
 * still fails with `golden recorded, re-run: <path>`; an existing golden is never overwritten.
 */
object GoldenFiles {
    /** Reads [relativePath] (for example `agenda/sessions.snapshot.json`) under the test data root. */
    fun read(relativePath: String): String {
        val root = requireRoot()
        val path = join(root, relativePath)
        return readFileOrNull(path) ?: fail("test data file not found: $path")
    }

    /** Compares [actualJson] with the golden [relativePath]; see the object documentation. */
    fun assertMatches(relativePath: String, actualJson: String) {
        assertMatches(requireRoot(), relativePath, actualJson, recordGoldens())
    }

    internal fun assertMatches(root: String, relativePath: String, actualJson: String, record: Boolean) {
        val path = join(root, relativePath)
        val expected = readFileOrNull(path)
        when {
            expected == null && record -> {
                writeFile(path, actualJson)
                fail("golden recorded, re-run: $relativePath")
            }

            expected == null -> fail("golden missing: $relativePath")
            expected != actualJson -> fail("golden mismatch: $relativePath\nexpected:\n$expected\nactual:\n$actualJson")
        }
    }

    private fun requireRoot(): String =
        testDataRoot()
            ?: fail(
                "test data root is not set: pass the system property devfest.domainTestdata " +
                    "(JVM, Android) or the environment variable DOMAIN_TESTDATA (iOS)"
            )

    private fun join(root: String, relativePath: String): String = root.trimEnd('/') + "/" + relativePath
}
