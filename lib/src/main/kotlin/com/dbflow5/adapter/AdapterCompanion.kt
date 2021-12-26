package com.dbflow5.adapter

import kotlin.reflect.KClass

/**
 * Description: Internal marker interface for adapter companion usages in KSP.
 */
interface AdapterCompanion<Model : Any> {

    val table: KClass<Model>
}
