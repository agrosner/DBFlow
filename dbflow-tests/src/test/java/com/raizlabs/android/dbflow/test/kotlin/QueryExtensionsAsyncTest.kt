package com.raizlabs.android.dbflow.test.kotlin

import android.os.Build
import com.raizlabs.android.dbflow.config.DatabaseConfig
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.kotlinextensions.*
import com.raizlabs.android.dbflow.sql.queriable.AsyncQuery
import com.raizlabs.android.dbflow.structure.AsyncModel
import com.raizlabs.android.dbflow.test.BuildConfig
import com.raizlabs.android.dbflow.test.ImmediateTransactionManager
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Description:
 */
@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(Build.VERSION_CODES.LOLLIPOP))
class QueryExtensionsAsyncTest {

    @Before
    fun setup_test() {
        FlowManager.init(FlowConfig.Builder(RuntimeEnvironment.application)
                .addDatabaseConfig(DatabaseConfig.Builder(KotlinDatabase::class.java)
                        .transactionManagerCreator { ImmediateTransactionManager(it) }
                        .build())
                .build())
    }

    @Test
    fun test_asyncMethodCall() {
        val model = KotlinModel().async
        assertTrue(model is AsyncModel)
    }

    @Test
    fun test_asyncQuery() {
        val query = (select from KotlinModel::class).async
        assertTrue(query is AsyncQuery)
    }

    @Test
    fun test_canGetList() {
        var called = false
        (select from KotlinModel::class).async list { transaction, list -> called = true }
        assertTrue(called)
    }

    @Test
    fun test_canGetResult() {
        var called = false
        (select from KotlinModel::class).async result { transaction, result -> called = true }
        assertTrue(called)
    }

    @Test
    fun test_getCursorResult() {
        var called = false
        (select from KotlinModel::class).async cursorResult { transaction, cursorResult ->
            called = true
            cursorResult.close()
        }
        assertTrue(called)
    }
}