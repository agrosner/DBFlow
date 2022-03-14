package com.dbflow5.adapter2

import com.dbflow5.adapter.AutoIncrementUpdater
import com.dbflow5.adapter.CompilableQuery
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.PrimaryModelClauseGetter
import com.dbflow5.adapter.QueryOps
import com.dbflow5.adapter.TableBinder
import com.dbflow5.adapter.TableOps
import com.dbflow5.adapter.TableOpsImpl
import com.dbflow5.adapter.TableSQL
import com.dbflow5.database.DatabaseHolder
import com.dbflow5.database.DatabaseObjectLookup
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.DatabaseConnection
import com.dbflow5.database.GeneratedDatabase
import com.dbflow5.database.ThreadLocalTransaction
import com.dbflow5.database.scope.WritableDatabaseScope
import com.dbflow5.observing.notifications.ModelNotification
import com.dbflow5.observing.notifications.ModelNotifier
import com.dbflow5.structure.ChangeAction
import com.dbflow5.transaction.TransactionDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
    private val modelNotifier = mock<ModelNotifier>()
    private val primaryModelClauseGetter = mock<PrimaryModelClauseGetter<Any>>()
    private val autoIncrementUpdater = mock<AutoIncrementUpdater<Any>>()
    private val dispatcher = UnconfinedTestDispatcher()
    private val generatedDatabase = mock<GeneratedDatabase> {
        on { transactionDispatcher } doReturn TransactionDispatcher(dispatcher)
        on { modelNotifier } doReturn modelNotifier
        on { threadLocalTransaction } doReturn ThreadLocalTransaction()
    }
    private val database = mock<DatabaseConnection> {
        on { generatedDatabase } doReturn generatedDatabase
    }

    private val queryOps = mock<QueryOps<Any>>()
    private lateinit var mockModelAdapter: ModelAdapter<Any>

    private lateinit var ops: TableOps<Any>

    @BeforeTest
    fun createOps() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        whenever(generatedDatabase.writableScope) doReturn WritableDatabaseScope(generatedDatabase)
        ops = TableOpsImpl(
            table = Any::class,
            queryOps = queryOps,
            tableSQL = tableSQL,
            tableBinder = tableBinder,
            primaryModelClauseGetter = primaryModelClauseGetter,
            autoIncrementUpdater = autoIncrementUpdater,
            notifyChanges = true,
        )

        mockModelAdapter = ModelAdapter(
            table = Any::class,
            ops = ops,
            propertyGetter = mock(),
            name = "Mock",
            creationSQL = CompilableQuery(""),
            createWithDatabase = false,
            primaryModelClauseGetter = mock(),
        )
        DatabaseObjectLookup.loadHolder {
            DatabaseHolder(
                tables = setOf(mockModelAdapter),
                views = setOf(),
                queries = setOf(),
            )
        }
    }

    @Test
    fun saveTest() = runTest(dispatcher) {
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
        verify(modelNotifier).onChange(
            ModelNotification.ModelChange(
                changedFields = listOf(),
                action = ChangeAction.CHANGE,
                adapter = mockModelAdapter,
            )
        )
    }

    @Test
    fun insertTest() = runTest(dispatcher) {
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
        verify(modelNotifier).onChange(
            ModelNotification.ModelChange(
                changedFields = listOf(),
                action = ChangeAction.INSERT,
                adapter = mockModelAdapter,
            )
        )
    }

    @Test
    fun updateTest() = runTest(dispatcher) {
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
        verify(modelNotifier).onChange(
            ModelNotification.ModelChange(
                changedFields = listOf(),
                action = ChangeAction.UPDATE,
                adapter = mockModelAdapter,
            )
        )
    }

    @Test
    fun deleteTest() = runTest(dispatcher) {
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
        verify(modelNotifier).onChange(
            ModelNotification.ModelChange(
                changedFields = listOf(),
                action = ChangeAction.DELETE,
                adapter = mockModelAdapter,
            )
        )
    }

    private companion object {
        const val ID = 1L
    }
}
