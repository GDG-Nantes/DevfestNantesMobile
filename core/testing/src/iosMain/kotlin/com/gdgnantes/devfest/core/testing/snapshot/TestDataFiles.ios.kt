@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.gdgnantes.devfest.core.testing.snapshot

import platform.Foundation.NSFileManager
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

internal actual fun testDataRoot(): String? = NSProcessInfo.processInfo.environment["DOMAIN_TESTDATA"] as? String

internal actual fun readFileOrNull(path: String): String? =
    NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)

internal actual fun writeFile(path: String, content: String) {
    val parent = NSURL.fileURLWithPath(path).URLByDeletingLastPathComponent?.path
    if (parent != null) {
        NSFileManager.defaultManager.createDirectoryAtPath(parent, true, null, null)
    }
    NSString.create(string = content).writeToFile(path, true, NSUTF8StringEncoding, null)
}

internal actual fun recordGoldens(): Boolean =
    NSProcessInfo.processInfo.environment["DOMAIN_RECORD_GOLDENS"] as? String == "true"
