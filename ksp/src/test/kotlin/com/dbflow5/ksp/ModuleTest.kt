package com.dbflow5.ksp

import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule

/**
 * Description:
 */
class ModuleTest : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(module)
    }

    @Test
    fun `verify module structure`() {
        koinTestRule.koin
    }
}