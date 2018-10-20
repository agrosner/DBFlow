package com.dbflow5

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
abstract class BaseUnitTest {

    @JvmField
    @Rule
    var dblflowTestRule = DBFlowTestRule.create()

    val context: Context
        get() = ApplicationProvider.getApplicationContext()
}