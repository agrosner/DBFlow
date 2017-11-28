package com.raizlabs.android.dbflow.rx.query

import android.database.Cursor
import com.raizlabs.android.dbflow.query.BaseQueriable
import com.raizlabs.android.dbflow.query.Queriable
import com.raizlabs.android.dbflow.database.DatabaseStatement
import rx.Single
import rx.Single.fromCallable

/**
 * Description: Represents [BaseQueriable] with RX constructs.
 */
open class RXQueriableImpl internal constructor(
        private val innerQueriable: Queriable) : RXQueriable {

    override fun query(): Single<Cursor> = fromCallable { innerQueriable.query() }

    override fun compileStatement(): Single<DatabaseStatement> =
            fromCallable { innerQueriable.compileStatement() }

    override fun longValue(): Single<Long> = fromCallable { innerQueriable.longValue() }

    override fun executeInsert(): Single<Long> =
            fromCallable { innerQueriable.executeInsert() }

    override fun executeUpdateDelete(): Single<Long> =
            fromCallable { innerQueriable.executeUpdateDelete() }

    override fun hasData(): Single<Boolean> = fromCallable { innerQueriable.hasData() }

    override fun execute(): Single<Void> {
        return fromCallable {
            innerQueriable.execute()
            null
        }
    }
}
