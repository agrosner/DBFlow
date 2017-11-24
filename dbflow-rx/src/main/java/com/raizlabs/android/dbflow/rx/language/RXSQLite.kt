@file:JvmName("RXSQLite")

package com.raizlabs.android.dbflow.rx.language

import com.raizlabs.android.dbflow.sql.language.BaseQueriable
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable
import com.raizlabs.android.dbflow.sql.queriable.Queriable

fun <T : Any> ModelQueriable<T>.rx(): RXModelQueriableImpl<T> = RXModelQueriableImpl<T>(this)

fun Queriable.rx(): RXQueriableImpl = RXQueriableImpl(this)

fun <T : Any> BaseQueriable<T>.rxBaseQueriable() = rx()
