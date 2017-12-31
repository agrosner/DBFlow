@file:JvmName("RXModelQueriable")

package com.raizlabs.dbflow5.rx2.query

import com.raizlabs.dbflow5.config.DBFlowDatabase
import com.raizlabs.dbflow5.query.ModelQueriable
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

fun <T : Any> ModelQueriable<T>.queryStreamResults(dbFlowDatabase: DBFlowDatabase): Flowable<T> =
    CursorResultFlowable(this, dbFlowDatabase)

fun <T : Any> ModelQueriable<T>.observeOnTableChanges(): Flowable<ModelQueriable<T>> =
    Flowable.create(TableChangeOnSubscribe(this), BackpressureStrategy.LATEST)