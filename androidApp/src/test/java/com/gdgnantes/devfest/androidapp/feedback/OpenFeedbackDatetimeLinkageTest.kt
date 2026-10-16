package com.gdgnantes.devfest.androidapp.feedback

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Guards G-05-3: OpenFeedback 1.0.0-alpha.3 was compiled against kotlinx-datetime 0.6.x and links
 * directly against `kotlinx.datetime.Instant`, `Instant.Companion`, `Clock.System` and
 * `toLocalDateTime(kotlinx.datetime.Instant, TimeZone)`. Plain kotlinx-datetime 0.8.0 only keeps
 * those names as source-level typealiases, so on the app runtime classpath the session detail
 * feedback area crashes with `NoClassDefFoundError`. Only the `0.8.0-0.6.x-compat` artifact still
 * ships the classes (see the comment above `kotlinxDatetime` in `gradle/libs.versions.toml`).
 *
 * This test runs OpenFeedback's own code on `:androidApp`'s unit-test runtime classpath, so it runs
 * in CI's `testDebugUnitTest` although CI builds keep OPEN_FEEDBACK_ENABLED off and never render
 * the OpenFeedback composable. It needs neither that flag nor Remote Config. The three tests cover
 * the three paths that failed on the device: the Firestore timestamp mapper (the crash frame),
 * comment posting (`Clock.System`) and the comment display mapping (`toLocalDateTime`).
 *
 * Everything is reached by reflection. The OpenFeedback functions are Kotlin-internal and their
 * signatures name the deprecated kotlinx-datetime types, so a compile-time reference would break
 * this file on a kotlinx-datetime version without those classes instead of failing at link time
 * the way the device does.
 *
 * On an OpenFeedback bump, re-point the reflected class and function names after inspecting the
 * new AARs with `javap`. Delete this guard together with the compat pin once OpenFeedback ships on
 * `kotlin.time.Instant`.
 */
class OpenFeedbackDatetimeLinkageTest {
    @Test
    fun firestore_timestamp_maps_to_an_instant() {
        val timestamp = newInstance("com.google.firebase.Timestamp", 1_760_605_200L, 0)

        val instant =
            callStatic(
                "io.openfeedback.mappers.FirestoreToModelMappers_androidKt",
                "timestampToInstant",
                timestamp,
            )

        assertEquals("2025-10-16T09:00:00Z", instant.toString())
    }

    @Test
    fun posting_comments_stamps_and_sorts_them_with_the_system_clock() {
        val first = commitComment(sessionData(userId = "user-a"), "Great talk")

        val firstComments = comments(first)
        assertEquals(1, firstComments.size)
        assertEquals("Great talk", call(firstComments.single(), "getText"))
        assertEquals("user-a", call(firstComments.single(), "getUserId"))
        assertNotNull(call(firstComments.single(), "getCreatedAt"))
        // Comment.hashCode reaches Instant.hashCode, one of the removed members.
        first.hashCode()

        val second = commitComment(sessionData(userId = "user-b", comments = firstComments), "Me too")

        val secondComments = comments(second)
        assertEquals(2, secondComments.size)
        // Order is not asserted: two Clock.System.now() readings can be equal.
        assertEquals(setOf("user-a", "user-b"), secondComments.map { call(it, "getUserId") }.toSet())
    }

    @Test
    fun posted_comment_is_formatted_for_display() {
        val posted = commitComment(sessionData(userId = "user-a"), "Great talk")

        val feedback =
            callStatic(
                "io.openfeedback.viewmodels.mappers.SessionDataToUiModelsKt",
                "toUISessionFeedback",
                posted,
                "en",
                null,
                null,
            )

        val uiComments = call(feedback, "getComments") as List<*>
        assertEquals(1, uiComments.size)
        val uiComment = uiComments.single()!!
        assertEquals("Great talk", call(uiComment, "getMessage"))
        val createdAt = call(uiComment, "getCreatedAt") as String
        assertFalse(createdAt.isBlank())
        assertTrue(createdAt.any { it.isDigit() })
    }

    private fun sessionData(
        userId: String,
        comments: List<Any> = emptyList(),
    ): Any {
        val project = newInstance("io.openfeedback.model.Project", emptyList<Any>(), emptyList<Any>())
        return newInstance(
            "io.openfeedback.model.SessionData",
            project,
            userId,
            emptySet<String>(),
            emptySet<String>(),
            emptyMap<String, Long>(),
            comments,
        )
    }

    private fun commitComment(
        sessionData: Any,
        text: String,
    ): Any =
        callStatic(
            "io.openfeedback.extensions.SessionData_extKt",
            "commitComment",
            sessionData,
            text,
        )

    private fun comments(sessionData: Any): List<Any> = (call(sessionData, "getComments") as List<*>).filterNotNull()

    /** Calls the public no-argument getter [name] on [target]. */
    private fun call(
        target: Any,
        name: String,
    ): Any? = invoke(target.javaClass.getMethod(name), target)

    /** Calls the static method [name] of [className]; the method is found by name and arity. */
    private fun callStatic(
        className: String,
        name: String,
        vararg args: Any?,
    ): Any {
        val method =
            Class
                .forName(className)
                .methods
                .single { it.name == name && it.parameterCount == args.size }
        return checkNotNull(invoke(method, null, *args)) { "$className.$name returned null" }
    }

    /** Builds [className] through its only public constructor of matching arity. */
    private fun newInstance(
        className: String,
        vararg args: Any?,
    ): Any {
        val constructor =
            Class
                .forName(className)
                .constructors
                .single { it.parameterCount == args.size }
        return unwrap { constructor.newInstance(*args) }
    }

    private fun invoke(
        method: Method,
        target: Any?,
        vararg args: Any?,
    ): Any? = unwrap { method.invoke(target, *args) }

    /**
     * Rethrows the cause of an [InvocationTargetException], so that a failure reads
     * `NoClassDefFoundError: kotlinx/datetime/...` and not an opaque reflection wrapper.
     */
    private fun <T> unwrap(block: () -> T): T =
        try {
            block()
        } catch (e: InvocationTargetException) {
            throw e.targetException ?: e
        }
}
