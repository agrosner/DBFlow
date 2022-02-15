package com.dbflow5.database

import com.dbflow5.query.BaseQueriable
import com.dbflow5.runtime.ModelNotification
import com.dbflow5.runtime.NotifyDistributor
import com.dbflow5.structure.ChangeAction
import kotlin.reflect.KClass

/**
 * Description: Delegates all of its calls to the contained [DatabaseStatement], while
 * providing notification methods for when operations occur.
 */
class DatabaseStatementWrapper<T : Any>(
    private val databaseStatement: DatabaseStatement,
    private val action: ChangeAction,
    private val table: KClass<T>,
    private val databaseWrapper: DatabaseWrapper,
) : DatabaseStatement {

    constructor(
        databaseStatement: DatabaseStatement,
        modelQueriable: BaseQueriable<T>,
        databaseWrapper: DatabaseWrapper,
    ) : this(databaseStatement, modelQueriable.primaryAction, modelQueriable.table, databaseWrapper)

    override fun executeUpdateDelete(): Long {
        val affected = databaseStatement.executeUpdateDelete()
        if (affected > 0) {
            NotifyDistributor(databaseWrapper)
                .onChange(
                    ModelNotification.TableChange(
                        table,
                        action
                    )
                )
        }
        return affected
    }

    override fun executeInsert(): Long {
        val affected = databaseStatement.executeInsert()
        if (affected > 0) {
            NotifyDistributor(databaseWrapper)
                .onChange(
                    ModelNotification.TableChange(
                        table, action
                    )
                )
        }
        return affected
    }

    override fun execute() = databaseStatement.execute()

    override fun close() = databaseStatement.close()

    override fun simpleQueryForLong(): Long = databaseStatement.simpleQueryForLong()

    override fun simpleQueryForString(): String? = databaseStatement.simpleQueryForString()

    override fun bindString(index: Int, s: String) = databaseStatement.bindString(index, s)

    override fun bindNull(index: Int) = databaseStatement.bindNull(index)

    override fun bindLong(index: Int, aLong: Long) = databaseStatement.bindLong(index, aLong)

    override fun bindDouble(index: Int, aDouble: Double) =
        databaseStatement.bindDouble(index, aDouble)

    override fun bindBlob(index: Int, bytes: ByteArray) = databaseStatement.bindBlob(index, bytes)

    override fun bindAllArgsAsStrings(selectionArgs: Array<String>?) =
        databaseStatement.bindAllArgsAsStrings(selectionArgs)
}
