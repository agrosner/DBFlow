package com.dbflow5.structure

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.ExecutableQuery
import com.dbflow5.query.SelectResult
import kotlinx.coroutines.runBlocking
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
fun <T : Any> oneToMany(
    getDb: () -> DatabaseWrapper,
    query: () -> ExecutableQuery<SelectResult<T>>
) =
    OneToMany(getDb, query)

/**
 * Description: Wraps a [OneToMany] annotation getter into a concise property setter.
 */
@Deprecated(
    "This class encourages main thread reading. Use the @OneToManyRelation " +
        "annotation to generate better safety for you."
)
class OneToMany<T : Any>(
    private val getDb: () -> DatabaseWrapper,
    private val query: () -> ExecutableQuery<SelectResult<T>>
) :
    ReadOnlyProperty<Any, List<T>?> {

    private var list: List<T>? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): List<T>? {
        if (list?.isEmpty() != false) {
            list = runBlocking { query().execute(getDb()).list() }
        }
        return list
    }
}
