package com.gdgnantes.devfest.androidapp.di

import android.app.Application
import android.content.Context
import com.gdgnantes.devfest.shared.di.sharedModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.MissingKoinDefinitionException
import org.koin.test.verify.verify
import kotlin.test.Test
import kotlin.test.assertFailsWith

@OptIn(KoinExperimentalAPI::class)
class AndroidGraphVerifyTest {
    // Context and Application are supplied at runtime by androidContext(), not by a module.
    private val androidRuntimeTypes = listOf(Context::class, Application::class)

    @Test
    fun full_android_graph_is_complete() {
        module { includes(sharedModules + androidModules) }.verify(extraTypes = androidRuntimeTypes)
    }

    @Test
    fun android_modules_alone_are_incomplete_so_verify_really_bites() {
        assertFailsWith<MissingKoinDefinitionException> {
            module { includes(androidModules) }.verify(extraTypes = androidRuntimeTypes)
        }
    }
}
