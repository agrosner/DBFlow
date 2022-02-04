package com.raizlabs.dbflow5.processor.test

import com.dbflow5.processor.DBFlowKaptProcessor
import com.dbflow5.processor.getModule
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
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
                mock(),
            )
        )
    }

    @Test
    fun `verify module structure`() {
        koinTestRule.koin
    }

    @Test
    fun `verify processor`() {
        inject<DBFlowKaptProcessor>().value
    }

    @AfterTest
    fun after() {
        stopKoin()
    }
}