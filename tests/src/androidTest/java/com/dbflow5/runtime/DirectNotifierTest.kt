package com.dbflow5.runtime

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dbflow5.TestDatabase
import com.dbflow5.config.FlowManager
import com.dbflow5.config.databaseForTable
import com.dbflow5.database.AndroidSQLiteOpenHelper
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.delete
import com.dbflow5.query.insertInto
import com.dbflow5.query.set
import com.dbflow5.query.update
import com.dbflow5.structure.ChangeAction
import com.dbflow5.structure.delete
import com.dbflow5.structure.insert
import com.dbflow5.structure.save
import com.dbflow5.structure.update
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class DirectNotifierTest {

    val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setupTest() {
        FlowManager.init(context) {
            database<TestDatabase>(
                {},
                AndroidSQLiteOpenHelper.createHelperCreator(context)
            )
        }
    }

    @Test
    fun validateCanNotifyDirect() = runBlockingTest {
        databaseForTable<SimpleModel> { db ->
            val simpleModel = SimpleModel("Name")

            val modelChange = mock<DirectModelNotifier.OnModelStateChangedListener<SimpleModel>>()
            DirectModelNotifier.get()
                .registerForModelStateChanges(SimpleModel::class.java, modelChange)

            simpleModel.insert(db)
            verify(modelChange).onModelChanged(simpleModel, ChangeAction.INSERT)

            simpleModel.update(db)
            verify(modelChange).onModelChanged(simpleModel, ChangeAction.UPDATE)

            simpleModel.save(db)
            verify(modelChange).onModelChanged(simpleModel, ChangeAction.CHANGE)

            simpleModel.delete(db)
            verify(modelChange).onModelChanged(simpleModel, ChangeAction.DELETE)
        }
    }

    @Test
    fun validateCanNotifyWrapperClasses() = runBlockingTest {
        databaseForTable<SimpleModel> { db ->
            val modelChange = Mockito.mock(OnTableChangedListener::class.java)
            DirectModelNotifier.get().registerForTableChanges(SimpleModel::class.java, modelChange)

            insertInto<SimpleModel>()
                .columnValues(SimpleModel_Table.name to "name")
                .executeInsert(db)

            verify(modelChange).onTableChanged(SimpleModel::class.java, ChangeAction.INSERT)

            (update<SimpleModel>() set SimpleModel_Table.name.eq("name2"))
                .executeUpdateDelete(db)

            verify(modelChange).onTableChanged(SimpleModel::class.java, ChangeAction.UPDATE)

            delete<SimpleModel>().executeUpdateDelete(db)

            verify(modelChange).onTableChanged(SimpleModel::class.java, ChangeAction.DELETE)
        }
    }

    @After
    fun teardown() {
        FlowManager.destroy()
    }
}