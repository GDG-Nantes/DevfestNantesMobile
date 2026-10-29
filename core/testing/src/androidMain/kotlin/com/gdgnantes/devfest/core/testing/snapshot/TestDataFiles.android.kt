package com.gdgnantes.devfest.core.testing.snapshot

import java.io.File

internal actual fun testDataRoot(): String? =
    System.getProperty("devfest.domainTestdata") ?: System.getenv("DOMAIN_TESTDATA")

internal actual fun readFileOrNull(path: String): String? =
    File(path).takeIf { it.isFile }?.readText(Charsets.UTF_8)

internal actual fun writeFile(path: String, content: String) {
    val file = File(path)
    file.parentFile?.mkdirs()
    file.writeText(content, Charsets.UTF_8)
}

internal actual fun recordGoldens(): Boolean = System.getProperty("devfest.recordGoldens") == "true"
