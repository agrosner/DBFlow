package com.dbflow5.test.runtime

import app.cash.turbine.test
import com.dbflow5.query.delete
import com.dbflow5.query.insert
import com.dbflow5.query.update
import com.dbflow5.runtime.DirectModelNotifier
import com.dbflow5.runtime.ModelNotification
import com.dbflow5.structure.ChangeAction
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.SimpleModel
import com.dbflow5.test.SimpleModel_Table
import com.dbflow5.test.TestDatabase_Database
import kotlin.test.Test
import kotlin.test.assertEquals

class DirectNotifierTest {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateCanNotifyDirect() = dbRule.runTest { testScope ->
        val model = SimpleModel("Name")
        (modelNotifier as DirectModelNotifier).notificationFlow
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
    fun validateCanNotifyWrapperClasses() = dbRule.runTest { testScope ->
        (modelNotifier as DirectModelNotifier).notificationFlow
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
