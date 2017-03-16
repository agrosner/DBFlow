package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.sql.language.property.PropertyFactory.from
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable

/**
 * Description: Provides some very nice Property class extensions.
 */

val Int.property
    get() = from(this)

val Char.property
    get() = from(this)

val Double.property
    get() = from(this)

val Long.property
    get() = from(this)

val Float.property
    get() = from(this)

val Short.property
    get() = from(this)

val Byte.property
    get() = from(this)

val <T : Any> T?.property
    get() = from(this)

val <T : Any> ModelQueriable<T>.property
    get() = from(this)

inline fun <reified T : Any> T.propertyString(stringRepresentation: String?) = from(T::class.java, stringRepresentation)