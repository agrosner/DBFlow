@file:JvmName("RXSQLite")

package com.raizlabs.android.dbflow.rx.language

import com.raizlabs.android.dbflow.query.BaseQueriable
import com.raizlabs.android.dbflow.query.ModelQueriable
import com.raizlabs.android.dbflow.query.Queriable

fun <T : Any> ModelQueriable<T>.rx(): RXModelQueriableImpl<T> = RXModelQueriableImpl<T>(this)

fun Queriable.rx(): RXQueriableImpl = RXQueriableImpl(this)

fun <T : Any> BaseQueriable<T>.rxBaseQueriable() = rx()
