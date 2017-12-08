@file:JvmName("RXSQLite")

package com.raizlabs.dbflow5.rx2.query

import com.raizlabs.dbflow5.query.ModelQueriable
import com.raizlabs.dbflow5.query.Queriable

fun <T : Any> ModelQueriable<T>.rx(): RXModelQueriableImpl<T> = RXModelQueriableImpl(this)

fun Queriable.rx(): RXQueriableImpl = RXQueriableImpl(this)

