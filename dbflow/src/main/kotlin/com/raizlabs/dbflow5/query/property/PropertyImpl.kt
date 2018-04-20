package com.raizlabs.dbflow5.query.property

import com.raizlabs.dbflow5.query.NameAlias
import kotlin.reflect.KClass

/**
 * Description:
 */
actual open class Property<T> : InternalProperty<T> {

    actual constructor(nameAlias: NameAlias) : super(nameAlias)

    actual constructor(table: KClass<*>?, nameAlias: NameAlias) : super(table, nameAlias)

    actual constructor(table: KClass<*>?, columnName: String?) : super(table, columnName)

    actual constructor(table: KClass<*>?, columnName: String, aliasName: String) : super(table, columnName, aliasName)


    constructor(table: Class<*>?, nameAlias: NameAlias) : super(table?.kotlin, nameAlias)

    constructor(table: Class<*>?, columnName: String?) : super(table?.kotlin, columnName)

    constructor(table: Class<*>?, columnName: String, aliasName: String) : super(table?.kotlin, columnName, aliasName)

    actual companion object {

        @JvmStatic
        actual val ALL_PROPERTY = InternalProperty.ALL_PROPERTY

        @JvmStatic
        actual val WILDCARD: Property<*> = InternalProperty.WILDCARD

        @JvmStatic
        fun allProperty(table: Class<*>): Property<String> = InternalProperty.allProperty(table.kotlin)

        @JvmStatic
        actual fun allProperty(table: KClass<*>) = InternalProperty.allProperty(table)

    }
}