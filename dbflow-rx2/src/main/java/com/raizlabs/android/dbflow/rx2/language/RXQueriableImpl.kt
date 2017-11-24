package com.raizlabs.android.dbflow.rx2.language

import android.database.Cursor
import com.raizlabs.android.dbflow.sql.language.BaseQueriable
import com.raizlabs.android.dbflow.sql.queriable.Queriable
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
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
