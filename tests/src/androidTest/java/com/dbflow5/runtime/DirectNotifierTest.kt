package com.dbflow5.runtime

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.dbflow5.TestDatabase_Database
import com.dbflow5.TestTransactionDispatcherFactory
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.delete
import com.dbflow5.query.insert
import com.dbflow5.query.update
import com.dbflow5.simpleModelAdapter
import com.dbflow5.structure.ChangeAction
import com.dbflow5.test.DatabaseTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class DirectNotifierTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestDatabase_Database) {
        copy(transactionDispatcherFactory = TestTransactionDispatcherFactory())
    }

    @Test
    fun validateCanNotifyDirect() = dbRule.runBlockingTest {
        NotifyDistributor.setNotifyDistributor(NotifyDistributorImpl(scope = this))
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

                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun validateCanNotifyWrapperClasses() = dbRule.runBlockingTest {
        DirectModelNotifier.get(this.db).notificationFlow
            .test {
                simpleModelAdapter.insert(SimpleModel_Table.name to "name")
                    .execute()

                assertEquals(
                    ModelNotification.TableChange(
                        simpleModelAdapter,
                        ChangeAction.INSERT,
                    ),
                    awaitItem(),
                )

                (simpleModelAdapter.update() set SimpleModel_Table.name.eq("name2"))
                    .execute()

                assertEquals(
                    ModelNotification.TableChange(
                        simpleModelAdapter,
                        ChangeAction.UPDATE,
                    ),
                    awaitItem(),
                )
                simpleModelAdapter.delete().execute()

                assertEquals(
                    ModelNotification.TableChange(
                        simpleModelAdapter,
                        ChangeAction.DELETE,
                    ),
                    awaitItem()
                )
            }
    }
}
