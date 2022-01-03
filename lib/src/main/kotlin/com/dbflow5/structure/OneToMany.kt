package com.dbflow5.structure

import com.dbflow5.config.FlowManager
import com.dbflow5.query.ModelQueriable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Description:
 */
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated(
    "This class encourages main thread reading. Use the @OneToManyRelation " +
        "annotation to generate better safety for you."
)
fun <T : Any> oneToMany(query: () -> ModelQueriable<T>) = OneToMany(query)

/**
 * Description: Wraps a [OneToMany] annotation getter into a concise property setter.
 */
@Deprecated(
    "This class encourages main thread reading. Use the @OneToManyRelation " +
        "annotation to generate better safety for you."
)
class OneToMany<T : Any>(private val query: () -> ModelQueriable<T>) :
    ReadOnlyProperty<Any, List<T>?> {

    private var list: List<T>? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): List<T>? {
        if (list?.isEmpty() != false) {
            list = query().queryList(FlowManager.getDatabaseForTable(thisRef::class.java))
        }
        return list
    }
}
