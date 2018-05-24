package com.raizlabs.dbflow5

import org.junit.Rule

abstract class BaseInstrumentedUnitTest {

    @JvmField
    @Rule
    var dblflowTestRule = DBFlowInstrumentedTestRule.create()

}