package com.raizlabs.android.dbflow.rx.language

import com.raizlabs.android.dbflow.query.list.FlowCursorList
import com.raizlabs.android.dbflow.query.list.FlowQueryList
import com.raizlabs.android.dbflow.query.BaseModelQueriable
import com.raizlabs.android.dbflow.query.CursorResult
import com.raizlabs.android.dbflow.query.ModelQueriable
import rx.Emitter
import rx.Observable
import rx.Single
import rx.Single.fromCallable

/**
 * Description: Represents [BaseModelQueriable] in RX form.
 */
class RXModelQueriableImpl<T : Any> internal
constructor(private val innerModelQueriable: ModelQueriable<T>)
    : RXQueriableImpl(innerModelQueriable), RXModelQueriable<T> {

    override fun queryStreamResults(): Observable<T> =
            Observable.create(CursorResultSubscriber(this))

    override fun queryResults(): Single<CursorResult<T>> =
            fromCallable { innerModelQueriable.queryResults() }

    override fun queryList(): Single<List<T>> = fromCallable { innerModelQueriable.queryList() }

    override fun querySingle(): Single<T> = fromCallable { innerModelQueriable.querySingle() }

    override fun cursorList(): Single<FlowCursorList<T>> =
            fromCallable { innerModelQueriable.cursorList() }

    override fun flowQueryList(): Single<FlowQueryList<T>> =
            fromCallable { innerModelQueriable.flowQueryList() }

    override fun <TQueryModel : Any> queryCustomList(
            queryModelClass: Class<TQueryModel>): Single<List<TQueryModel>> =
            fromCallable { innerModelQueriable.queryCustomList(queryModelClass) }

    override fun <TQueryModel : Any> queryCustomSingle(
            queryModelClass: Class<TQueryModel>): Single<TQueryModel> =
            fromCallable { innerModelQueriable.queryCustomSingle(queryModelClass) }

    override fun observeOnTableChanges(): Observable<ModelQueriable<T>> {
        return Observable.create(TableChangeListenerEmitter(innerModelQueriable),
                Emitter.BackpressureMode.LATEST)
    }
}
