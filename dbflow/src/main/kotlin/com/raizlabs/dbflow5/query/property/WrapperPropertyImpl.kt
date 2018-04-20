package com.raizlabs.dbflow5.query.property

import com.raizlabs.dbflow5.query.NameAlias
import kotlin.reflect.KClass

actual open class WrapperProperty<T, V> : InternalWrapperProperty<T, V> {

    actual constructor(table: KClass<*>, nameAlias: NameAlias) : super(table, nameAlias)

    actual constructor(table: KClass<*>, columnName: String) : super(table, columnName)

    constructor(table: Class<*>, nameAlias: NameAlias) : super(table.kotlin, nameAlias)

    constructor(table: Class<*>, columnName: String) : super(table.kotlin, columnName)
}