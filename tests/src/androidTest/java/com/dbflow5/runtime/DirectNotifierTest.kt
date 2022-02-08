package com.dbflow5.runtime

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dbflow5.TestDatabase
import com.dbflow5.TestTransactionDispatcherFactory
import com.dbflow5.config.FlowManager
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.database.AndroidSQLiteOpenHelper
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query2.delete
import com.dbflow5.query2.insert
import com.dbflow5.query2.update
import com.dbflow5.simpleModelAdapter
import com.dbflow5.structure.ChangeAction
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DirectNotifierTest {

    val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setupTest() {
        FlowManager.init(context) {
            database<TestDatabase>({
                transactionDispatcherFactory(
                    TestTransactionDispatcherFactory(
                        TestCoroutineDispatcher()
                    )
                )
            }, AndroidSQLiteOpenHelper.createHelperCreator(context))
        }
    }

    @Test
    fun validateCanNotifyDirect() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            val model = SimpleModel("Name")

            val modelChange = mock<DirectModelNotifier.OnModelStateChangedListener<SimpleModel>>()
            DirectModelNotifier.get(this.db)
                .registerForModelStateChanges(SimpleModel::class, modelChange)

            simpleModelAdapter.insert(model)
            verify(modelChange).onModelChanged(model, ChangeAction.INSERT)

            simpleModelAdapter.update(model)
            verify(modelChange).onModelChanged(model, ChangeAction.UPDATE)

            simpleModelAdapter.save(model)
            verify(modelChange).onModelChanged(model, ChangeAction.CHANGE)

            simpleModelAdapter.delete(model)
            verify(modelChange).onModelChanged(model, ChangeAction.DELETE)
        }
    }

    @Test
    fun validateCanNotifyWrapperClasses() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            val modelChange = mock<OnTableChangedListener>()
            DirectModelNotifier.get(this.db)
                .registerForTableChanges(SimpleModel::class, modelChange)

            simpleModelAdapter.insert(SimpleModel_Table.name to "name")
                .execute()

            verify(modelChange).onTableChanged(SimpleModel::class, ChangeAction.INSERT)

            (simpleModelAdapter.update() set SimpleModel_Table.name.eq("name2"))
                .execute()

            verify(modelChange).onTableChanged(SimpleModel::class, ChangeAction.UPDATE)

            simpleModelAdapter.delete().execute()

            verify(modelChange).onTableChanged(SimpleModel::class, ChangeAction.DELETE)
        }
    }

    @After
    fun teardown() {
        FlowManager.destroy()
    }
}