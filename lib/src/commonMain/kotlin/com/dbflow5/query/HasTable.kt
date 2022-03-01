package com.dbflow5.query

import kotlin.reflect.KClass

interface HasTable<Table : Any> {

    val table: KClass<Table>
}
