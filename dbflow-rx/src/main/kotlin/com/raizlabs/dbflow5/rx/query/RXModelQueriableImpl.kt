package com.raizlabs.dbflow5.rx.query

import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.BaseModelQueriable
import com.raizlabs.dbflow5.query.ModelQueriable
import com.raizlabs.dbflow5.query.list.FlowCursorList
import com.raizlabs.dbflow5.query.list.FlowQueryList
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

    override fun queryStreamResults(databaseWrapper: DatabaseWrapper): Observable<T> =
        Observable.unsafeCreate(CursorResultSubscriber(this, databaseWrapper))

    override fun queryList(databaseWrapper: DatabaseWrapper): Single<List<T>> = fromCallable {
        innerModelQueriable.queryList(databaseWrapper)
    }

    override fun querySingle(databaseWrapper: DatabaseWrapper): Single<T> = fromCallable {
        innerModelQueriable.querySingle(databaseWrapper)
    }

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
        : Single<TQueryModel> =
        fromCallable { innerModelQueriable.queryCustomSingle(queryModelClass, databaseWrapper) }

    override fun observeOnTableChanges(): Observable<ModelQueriable<T>> {
        return Observable.create(TableChangeListenerEmitter(innerModelQueriable),
            Emitter.BackpressureMode.LATEST)
    }
}
