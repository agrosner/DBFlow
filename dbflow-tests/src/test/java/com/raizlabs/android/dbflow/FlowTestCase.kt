package com.raizlabs.android.dbflow

import android.content.Context
import android.os.Build

import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Description:
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(Build.VERSION_CODES.LOLLIPOP),
    assetDir = "build/intermediates/classes/test/")
abstract class FlowTestCase {

    @JvmField
    @Rule
    var dbFlowTestRule = DBFlowTestRule.create()

    protected val context: Context
        get() = RuntimeEnvironment.application
}
