package com.raizlabs.dbflow5.rx2.query

import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.BaseModelQueriable
import com.raizlabs.dbflow5.query.CursorResult
import com.raizlabs.dbflow5.query.ModelQueriable
import com.raizlabs.dbflow5.query.list.FlowCursorList
import com.raizlabs.dbflow5.query.list.FlowQueryList
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

    override fun queryStreamResults(databaseWrapper: DatabaseWrapper): Flowable<T> =
            CursorResultFlowable(this, databaseWrapper)

    override fun queryResults(databaseWrapper: DatabaseWrapper): Single<CursorResult<T>> =
            fromCallable { innerModelQueriable.queryResults(databaseWrapper) }

    override fun queryList(databaseWrapper: DatabaseWrapper): Single<List<T>> = fromCallable {
        innerModelQueriable.queryList(databaseWrapper)
    }

    override fun querySingle(databaseWrapper: DatabaseWrapper): Maybe<T> =
            Maybe.fromCallable { innerModelQueriable.querySingle(databaseWrapper) }

    override fun cursorList(databaseWrapper: DatabaseWrapper): Single<FlowCursorList<T>> =
            fromCallable { innerModelQueriable.cursorList(databaseWrapper) }

    override fun flowQueryList(databaseWrapper: DatabaseWrapper): Single<FlowQueryList<T>> =
            fromCallable { innerModelQueriable.flowQueryList(databaseWrapper) }

    override fun <TQueryModel : Any> queryCustomList(queryModelClass: Class<TQueryModel>,
                                                     databaseWrapper: DatabaseWrapper)
            : Single<List<TQueryModel>> =
            fromCallable { innerModelQueriable.queryCustomList(queryModelClass, databaseWrapper) }

    override fun <TQueryModel : Any> queryCustomSingle(queryModelClass: Class<TQueryModel>,
                                                       databaseWrapper: DatabaseWrapper)
            : Maybe<TQueryModel> =
            Maybe.fromCallable { innerModelQueriable.queryCustomSingle(queryModelClass, databaseWrapper) }

    override fun observeOnTableChanges(): Flowable<ModelQueriable<T>> =
            Flowable.create(TableChangeOnSubscribe(innerModelQueriable), BackpressureStrategy.LATEST)
}
