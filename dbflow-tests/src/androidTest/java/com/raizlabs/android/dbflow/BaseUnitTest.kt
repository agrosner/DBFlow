package com.raizlabs.android.dbflow

import org.junit.Rule

abstract class BaseInstrumentedUnitTest {

    @JvmField
    @Rule
    var dblflowTestRule = DBFlowInstrumentedTestRule.create()

}