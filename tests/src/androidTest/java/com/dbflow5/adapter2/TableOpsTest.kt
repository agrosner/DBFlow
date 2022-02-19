package com.dbflow5.adapter2

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.runtime.NotifyDistributor
import com.dbflow5.transaction.TransactionDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Description:
 */
class TableOpsTest {

    private val tableSQL = TableSQL(
        CompilableQuery(""),
        CompilableQuery(""),
        CompilableQuery(""),
        CompilableQuery(""),
    )
    private val tableBinder = TableBinder<Any>(
        insert = mock(),
        update = mock(),
        delete = mock(),
        save = mock(),
    )
    private val notifyDistributor = mock<NotifyDistributor>()
    private val primaryModelClauseGetter = mock<PrimaryModelClauseGetter<Any>>()
    private val autoIncrementUpdater = mock<AutoIncrementUpdater<Any>>()
    private val database = mock<DatabaseWrapper> {
        on { generatedDatabase } doReturn mock {
            on { transactionDispatcher } doReturn TransactionDispatcher(TestCoroutineDispatcher())
        }
    }

    private lateinit var ops: TableOps<Any>

    @BeforeTest
    fun createOps() {
        ops = TableOpsImpl(
            tableSQL,
            tableBinder,
            notifyDistributor,
            primaryModelClauseGetter,
            autoIncrementUpdater,
        )
    }

    @Test
    fun saveTest() = runBlockingTest {
        val model = mock<Any>()
        ops.apply {
            val retModel = database.save(model)
            assertEquals(model, retModel)
        }

        verify(tableSQL.save).create(database)
    }
}