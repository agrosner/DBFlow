package com.dbflow5.adapter2

import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.runtime.ModelNotification
import com.dbflow5.runtime.NotifyDistributor
import com.dbflow5.structure.ChangeAction
import com.dbflow5.transaction.TransactionDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
    private val generatedDatabase = mock<GeneratedDatabase> {
        on { transactionDispatcher } doReturn TransactionDispatcher(TestCoroutineDispatcher())
    }
    private val database = mock<DatabaseWrapper> {
        on { generatedDatabase } doReturn generatedDatabase
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
        val statement = mock<DatabaseStatement> {
            on { executeInsert() } doReturn 1
        }
        val model = mock<Any>()

        whenever(generatedDatabase.compileStatement(tableSQL.save.query)) doReturn statement
        whenever(primaryModelClauseGetter.get(model)) doReturn listOf()
        whenever(autoIncrementUpdater.run { model.update(1L) }) doReturn
            model

        ops.apply {
            val retModel = database.save(model)
            assertEquals(model, retModel)
        }

        verify(tableBinder.save).bind(statement, model)
        verify(statement).executeInsert()
        verify(autoIncrementUpdater).run { model.update(1L) }
        verify(notifyDistributor).onChange(
            ModelNotification.ModelChange(
                changedFields = listOf(),
                action = ChangeAction.CHANGE,
                table = model::class,
            )
        )
    }

    @Test
    fun insertTest() = runBlockingTest {
        val statement = mock<DatabaseStatement> {
            on { executeInsert() } doReturn 1
        }
        val model = mock<Any>()

        whenever(generatedDatabase.compileStatement(tableSQL.insert.query)) doReturn statement
        whenever(primaryModelClauseGetter.get(model)) doReturn listOf()
        whenever(autoIncrementUpdater.run { model.update(1L) }) doReturn
            model

        ops.apply {
            val retModel = database.insert(model)
            assertEquals(model, retModel)
        }

        verify(tableBinder.insert).bind(statement, model)
        verify(statement).executeInsert()
        verify(autoIncrementUpdater).run { model.update(1L) }
        verify(notifyDistributor).onChange(
            ModelNotification.ModelChange(
                changedFields = listOf(),
                action = ChangeAction.INSERT,
                table = model::class,
            )
        )
    }

    @Test
    fun updateTest() = runBlockingTest {
        val statement = mock<DatabaseStatement> {
            on { executeUpdateDelete() } doReturn ID
        }
        val model = mock<Any>()

        whenever(generatedDatabase.compileStatement(tableSQL.update.query)) doReturn statement
        whenever(primaryModelClauseGetter.get(model)) doReturn listOf()

        ops.apply {
            val retModel = database.update(model)
            assertEquals(model, retModel)
        }

        verify(tableBinder.update).bind(statement, model)
        verify(statement).executeUpdateDelete()
        verify(notifyDistributor).onChange(
            ModelNotification.ModelChange(
                changedFields = listOf(),
                action = ChangeAction.UPDATE,
                table = model::class,
            )
        )
    }

    @Test
    fun deleteTest() = runBlockingTest {
        val statement = mock<DatabaseStatement> {
            on { executeUpdateDelete() } doReturn ID
        }
        val model = mock<Any>()

        whenever(generatedDatabase.compileStatement(tableSQL.delete.query)) doReturn statement
        whenever(primaryModelClauseGetter.get(model)) doReturn listOf()

        ops.apply {
            val retModel = database.delete(model)
            assertEquals(model, retModel)
        }

        verify(tableBinder.delete).bind(statement, model)
        verify(statement).executeUpdateDelete()
        verify(notifyDistributor).onChange(
            ModelNotification.ModelChange(
                changedFields = listOf(),
                action = ChangeAction.DELETE,
                table = model::class,
            )
        )
    }

    private companion object {
        const val ID = 1L
    }
}