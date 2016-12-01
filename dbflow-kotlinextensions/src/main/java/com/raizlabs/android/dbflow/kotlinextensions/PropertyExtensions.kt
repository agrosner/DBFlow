package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.sql.language.property.*
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable

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

val <T : Any> T.property: Property<T>
    get() = PropertyFactory.from(this)

val <T : Any> ModelQueriable<T>.property: Property<T>
    get() = PropertyFactory.from(this)

inline fun <reified TModel : Any> TModel.propertyString(stringRepresentation: String?): Property<TModel> {
    return PropertyFactory.from(TModel::class.java, stringRepresentation)
}