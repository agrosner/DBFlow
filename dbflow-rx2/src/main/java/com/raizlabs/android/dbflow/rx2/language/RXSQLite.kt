@file:JvmName("RXSQLite")

package com.raizlabs.android.dbflow.rx2.language

import com.raizlabs.android.dbflow.query.ModelQueriable
import com.raizlabs.android.dbflow.query.Queriable

fun <T : Any> ModelQueriable<T>.rx(): RXModelQueriableImpl<T> = RXModelQueriableImpl(this)

fun Queriable.rx(): RXQueriableImpl = RXQueriableImpl(this)

