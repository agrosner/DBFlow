package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.sql.language.property.*

/**
 * Description: Provides some very nice Property class extensions.
 */

val Int.property: IntProperty
    get() = PropertyFactory.from(this)

val Char.property: CharProperty
    get() = PropertyFactory.from(this)

val Double.property: DoubleProperty
    get() = PropertyFactory.from(this)

val Long.property: LongProperty
    get() = PropertyFactory.from(this)

val Float.property: FloatProperty
    get() = PropertyFactory.from(this)

val Short.property: ShortProperty
    get() = PropertyFactory.from(this)

val Byte.property: ByteProperty
    get() = PropertyFactory.from(this)

val <T> T.property: Property<T>
    get() = PropertyFactory.from(this)

inline fun <reified T : Any?> propertyString(stringRepresentation: String?): Property<T> {
    return PropertyFactory.from(T::class.java, stringRepresentation)
}


