package com.raizlabs.android.dbflow

import android.content.Context
import android.os.Build
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(Build.VERSION_CODES.LOLLIPOP),
    assetDir = "build/intermediates/classes/test/")
abstract class BaseUnitTest {

    @JvmField
    @Rule
    var dblflowTestRule = DBFlowTestRule.create()

    val context: Context
        get() = RuntimeEnvironment.application
}