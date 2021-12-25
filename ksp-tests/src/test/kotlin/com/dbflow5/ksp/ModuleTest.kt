package com.dbflow5.ksp

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.mockito.kotlin.mock
import kotlin.test.AfterTest

/**
 * Description:
 */
class ModuleTest : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            getModule(
                SymbolProcessorEnvironment(
                    options = mapOf(),
                    kotlinVersion = KotlinVersion.CURRENT,
                    codeGenerator = mock(),
                    logger = mock()
                )
            )
        )
    }

    @Test
    fun `verify module structure`() {
        koinTestRule.koin
    }

    @AfterTest
    fun after() {
        stopKoin()
    }
}