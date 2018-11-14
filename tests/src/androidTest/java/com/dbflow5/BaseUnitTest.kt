package com.dbflow5

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule

abstract class BaseUnitTest {

    @JvmField
    @Rule
    var dblflowTestRule = DBFlowInstrumentedTestRule.create()

    val context: Context
        get() = ApplicationProvider.getApplicationContext()

}