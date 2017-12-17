package com.raizlabs.dbflow5.rx2.query

import com.raizlabs.dbflow5.database.DatabaseStatement
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor
import com.raizlabs.dbflow5.query.BaseQueriable
import com.raizlabs.dbflow5.query.Queriable
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.Single.fromCallable

/**
 * Description: Represents [BaseQueriable] with RX constructs.
 */
open class RXQueriableImpl(private val innerQueriable: Queriable) : RXQueriable {

    override fun cursor(databaseWrapper: DatabaseWrapper): Maybe<FlowCursor> = Maybe.fromCallable {
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

    override fun execute(databaseWrapper: DatabaseWrapper): Completable = Completable.fromRunnable {
        innerQueriable.execute(databaseWrapper)
    }

}
