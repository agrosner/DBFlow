package com.dbflow5

import android.content.Context
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
abstract class BaseUnitTest {

    @JvmField
    @Rule
    var dblflowTestRule = DBFlowTestRule.create()

    val context: Context
        get() = RuntimeEnvironment.application
}