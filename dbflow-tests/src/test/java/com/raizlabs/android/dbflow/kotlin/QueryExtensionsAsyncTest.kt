package com.raizlabs.android.dbflow.kotlin

import android.os.Build
import com.raizlabs.android.dbflow.config.DatabaseConfig
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.kotlinextensions.async
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.sql.queriable.AsyncQuery
import com.raizlabs.android.dbflow.structure.AsyncModel
import com.raizlabs.android.dbflow.BuildConfig
import com.raizlabs.android.dbflow.ImmediateTransactionManager
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Description:
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(Build.VERSION_CODES.LOLLIPOP))
class QueryExtensionsAsyncTest {

    @Before
    fun setup_test() {
        FlowManager.init(FlowConfig.Builder(RuntimeEnvironment.application)
            .addDatabaseConfig(DatabaseConfig.Builder(KotlinDatabase::class.java)
                .transactionManagerCreator(::ImmediateTransactionManager)
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
        assertTrue(query is AsyncQuery<*>)
    }
}