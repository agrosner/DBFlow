package com.raizlabs.dbflow5.rx.query

import com.raizlabs.dbflow5.database.DatabaseStatement
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor
import com.raizlabs.dbflow5.query.BaseQueriable
import com.raizlabs.dbflow5.query.Queriable
import rx.Single
import rx.Single.fromCallable

/**
 * Description: Represents [BaseQueriable] with RX constructs.
 */
open class RXQueriableImpl internal constructor(
        private val innerQueriable: Queriable) : RXQueriable {

    override fun query(databaseWrapper: DatabaseWrapper): Single<FlowCursor> = fromCallable {
        innerQueriable.cursor(databaseWrapper)
    }

    override fun compileStatement(databaseWrapper: DatabaseWrapper): Single<DatabaseStatement> =
            fromCallable { innerQueriable.compileStatement(databaseWrapper) }

    override fun longValue(databaseWrapper: DatabaseWrapper): Single<Long> = fromCallable {
        innerQueriable.longValue(databaseWrapper)
    }

    override fun executeInsert(databaseWrapper: DatabaseWrapper): Single<Long> =
            fromCallable { innerQueriable.executeInsert(databaseWrapper) }

    override fun executeUpdateDelete(databaseWrapper: DatabaseWrapper): Single<Long> =
            fromCallable { innerQueriable.executeUpdateDelete(databaseWrapper) }

    override fun hasData(databaseWrapper: DatabaseWrapper): Single<Boolean> = fromCallable {
        innerQueriable.hasData(databaseWrapper)
    }

    override fun execute(databaseWrapper: DatabaseWrapper): Single<Void> {
        return fromCallable {
            innerQueriable.execute(databaseWrapper)
            null
        }
    }
}
