package com.dbflow5.runtime

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.dbflow5.TestDatabase
import com.dbflow5.TestTransactionDispatcherFactory
import com.dbflow5.config.FlowManager
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.database.AndroidSQLiteOpenHelper
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.delete
import com.dbflow5.query.insert
import com.dbflow5.query.update
import com.dbflow5.simpleModelAdapter
import com.dbflow5.structure.ChangeAction
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

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
            DirectModelNotifier.get(this.db).notificationFlow
                .test {
                    simpleModelAdapter.insert(model)
                    assertEquals(
                        ModelNotification.ModelChange(
                            model,
                            ChangeAction.INSERT,
                            simpleModelAdapter,
                        ),
                        awaitItem()
                    )

                    simpleModelAdapter.update(model)
                    assertEquals(
                        ModelNotification.ModelChange(
                            model,
                            ChangeAction.UPDATE,
                            simpleModelAdapter,
                        ),
                        awaitItem()
                    )

                    simpleModelAdapter.save(model)
                    assertEquals(
                        ModelNotification.ModelChange(
                            model,
                            ChangeAction.CHANGE,
                            simpleModelAdapter,
                        ),
                        awaitItem()
                    )

                    simpleModelAdapter.delete(model)
                    assertEquals(
                        ModelNotification.ModelChange(
                            model,
                            ChangeAction.DELETE,
                            simpleModelAdapter,
                        ),
                        awaitItem()
                    )

                    awaitComplete()
                }
        }
    }

    @Test
    fun validateCanNotifyWrapperClasses() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            DirectModelNotifier.get(this.db).notificationFlow
                .test {
                    simpleModelAdapter.insert(SimpleModel_Table.name to "name")
                        .execute()

                    assertEquals(
                        ModelNotification.TableChange(
                            SimpleModel::class,
                            ChangeAction.INSERT,
                        ),
                        awaitItem(),
                    )

                    (simpleModelAdapter.update() set SimpleModel_Table.name.eq("name2"))
                        .execute()

                    assertEquals(
                        ModelNotification.TableChange(
                            SimpleModel::class,
                            ChangeAction.UPDATE,
                        ),
                        awaitItem(),
                    )
                    simpleModelAdapter.delete().execute()

                    assertEquals(
                        ModelNotification.TableChange(
                            SimpleModel::class,
                            ChangeAction.DELETE,
                        ),
                        awaitItem()
                    )
                }
        }
    }

    @After
    fun teardown() {
        FlowManager.destroy()
    }
}