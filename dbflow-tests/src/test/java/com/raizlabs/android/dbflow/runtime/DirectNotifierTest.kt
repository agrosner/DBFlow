package com.raizlabs.android.dbflow.runtime

import android.content.Context
import android.os.Build
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.raizlabs.android.dbflow.BuildConfig
import com.raizlabs.android.dbflow.ImmediateTransactionManager
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.config.DatabaseConfig
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.kotlinextensions.*
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table
import com.raizlabs.android.dbflow.structure.BaseModel
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(Build.VERSION_CODES.LOLLIPOP),
        assetDir = "build/intermediates/classes/test/")
class DirectNotifierTest {

    val context: Context
        get() = RuntimeEnvironment.application

    @Before
    fun setupTest() {
        FlowManager.init(FlowConfig.Builder(context)
                .addDatabaseConfig(DatabaseConfig.Builder(TestDatabase::class.java)
                        .transactionManagerCreator(::ImmediateTransactionManager)
                        .modelNotifier(DirectModelNotifier.get())
                        .build()).build())
    }

    @Test
    fun validateCanNotifyDirect() {
        val simpleModel = SimpleModel("Name")

        val modelChange = mock<DirectModelNotifier.OnModelStateChangedListener<SimpleModel>>()
        DirectModelNotifier.get().registerForModelStateChanges(SimpleModel::class.java, modelChange)

        simpleModel.insert()
        verify(modelChange).onModelChanged(simpleModel, BaseModel.Action.INSERT)

        simpleModel.update()
        verify(modelChange).onModelChanged(simpleModel, BaseModel.Action.UPDATE)

        simpleModel.save()
        verify(modelChange, times(2)).onModelChanged(simpleModel, BaseModel.Action.UPDATE)

        simpleModel.delete()
        verify(modelChange).onModelChanged(simpleModel, BaseModel.Action.DELETE)
    }

    @Test
    fun validateCanNotifyWrapperClasses() {
        val modelChange = mock<OnTableChangedListener>()
        DirectModelNotifier.get().registerForTableChanges(SimpleModel::class.java, modelChange)

        insert<SimpleModel>().columnValues(SimpleModel_Table.name to "name").executeInsert()

        verify(modelChange).onTableChanged(SimpleModel::class.java, BaseModel.Action.INSERT)

        (update<SimpleModel>() set SimpleModel_Table.name.eq("name2")).executeUpdateDelete()

        verify(modelChange).onTableChanged(SimpleModel::class.java, BaseModel.Action.UPDATE)

        delete<SimpleModel>().executeUpdateDelete()

        verify(modelChange).onTableChanged(SimpleModel::class.java, BaseModel.Action.DELETE)
    }

    @After
    fun teardown() {
        FlowManager.destroy()
    }
}