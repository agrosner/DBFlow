package com.raizlabs.android.dbflow.rx2.language

import com.raizlabs.android.dbflow.list.FlowCursorList
import com.raizlabs.android.dbflow.list.FlowQueryList
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable
import com.raizlabs.android.dbflow.sql.language.CursorResult
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.Single.fromCallable

/**
 * Description: Represents [BaseModelQueriable] in RX form.
 */
class RXModelQueriableImpl<T : Any>(private val innerModelQueriable: ModelQueriable<T>)
    : RXQueriableImpl(innerModelQueriable), RXModelQueriable<T> {

    override val table: Class<T>
        get() = innerModelQueriable.table

    override fun queryStreamResults(): Flowable<T> = CursorResultFlowable(this)

    override fun queryResults(): Single<CursorResult<T>> =
            fromCallable { innerModelQueriable.queryResults() }

    override fun queryList(): Single<List<T>> = fromCallable { innerModelQueriable.queryList() }

    override fun querySingle(): Maybe<T> = Maybe.fromCallable { innerModelQueriable.querySingle() }

    override fun cursorList(): Single<FlowCursorList<T>> =
            fromCallable { innerModelQueriable.cursorList() }

    override fun flowQueryList(): Single<FlowQueryList<T>> =
            fromCallable { innerModelQueriable.flowQueryList() }

    override fun <TQueryModel : Any> queryCustomList(
            queryModelClass: Class<TQueryModel>): Single<List<TQueryModel>> =
            fromCallable { innerModelQueriable.queryCustomList(queryModelClass) }

    override fun <TQueryModel : Any> queryCustomSingle(
            queryModelClass: Class<TQueryModel>): Maybe<TQueryModel> =
            Maybe.fromCallable { innerModelQueriable.queryCustomSingle(queryModelClass) }

    override fun observeOnTableChanges(): Flowable<ModelQueriable<T>> =
            Flowable.create(TableChangeOnSubscribe(innerModelQueriable), BackpressureStrategy.LATEST)
}
