package com.gdgnantes.devfest.core.testing.snapshot

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoldenFilesTest {
    private val root: File = Files.createTempDirectory("golden-files-test").toFile()
    private val golden = File(root, "agenda/golden/sample.json")

    @AfterTest
    fun cleanUp() {
        root.deleteRecursively()
    }

    private fun check(record: Boolean, actual: String) =
        GoldenFiles.assertMatches(root.path, "agenda/golden/sample.json", actual, record)

    private fun failureOf(record: Boolean, actual: String): String =
        assertFailsWith<AssertionError> { check(record, actual) }.message.orEmpty()

    @Test
    fun an_absent_golden_fails_a_default_run_without_writing() {
        assertTrue(failureOf(record = false, actual = "{}\n").startsWith("golden missing: agenda/golden/sample.json"))
        assertFalse(golden.exists())
    }

    @Test
    fun an_absent_golden_is_written_in_record_mode_and_the_run_still_fails() {
        assertTrue(failureOf(record = true, actual = "{}\n").startsWith("golden recorded, re-run:"))
        assertEquals("{}\n", golden.readText())
    }

    @Test
    fun an_equal_golden_passes_in_both_modes() {
        golden.parentFile.mkdirs()
        golden.writeText("{}\n")

        check(record = false, actual = "{}\n")
        check(record = true, actual = "{}\n")
    }

    @Test
    fun a_different_golden_fails_and_is_never_overwritten() {
        golden.parentFile.mkdirs()
        golden.writeText("{}\n")

        assertTrue(failureOf(record = false, actual = "[]\n").startsWith("golden mismatch:"))
        assertTrue(failureOf(record = true, actual = "[]\n").startsWith("golden mismatch:"))
        assertEquals("{}\n", golden.readText())
    }
}
