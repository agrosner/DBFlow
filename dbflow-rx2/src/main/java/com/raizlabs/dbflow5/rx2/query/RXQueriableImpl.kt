package com.raizlabs.dbflow5.rx2.query

import android.database.Cursor
import com.raizlabs.dbflow5.query.BaseQueriable
import com.raizlabs.dbflow5.query.Queriable
import com.raizlabs.dbflow5.database.DatabaseStatement
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.Single.fromCallable

/**
 * Description: Represents [BaseQueriable] with RX constructs.
 */
open class RXQueriableImpl(private val innerQueriable: Queriable) : RXQueriable {

    override fun query(): Maybe<Cursor> = Maybe.fromCallable { innerQueriable.query() }

    override fun compileStatement(): Single<DatabaseStatement> =
            fromCallable { innerQueriable.compileStatement() }

    override fun longValue(): Single<Long> = fromCallable { innerQueriable.longValue() }

    override fun executeInsert(): Single<Long> =
            fromCallable { innerQueriable.executeInsert() }

    override fun executeUpdateDelete(): Single<Long> =
            fromCallable { innerQueriable.executeUpdateDelete() }

    override fun hasData(): Single<Boolean> = fromCallable { innerQueriable.hasData() }

    override fun execute(): Completable = Completable.fromRunnable { innerQueriable.execute() }

}
