package com.dbflow5.query

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.property.IProperty
import kotlin.reflect.KClass

/**
 * Description: Combines basic transformations and query ops into a base class.
 */
abstract class BaseTransformable<TModel : Any>
/**
 * Constructs new instance of this class and is meant for subclasses only.
 *
 * @param table the table that belongs to this query.
 */
protected constructor(table: KClass<TModel>) : BaseModelQueriable<TModel>(table),
    Transformable<TModel>, WhereBase<TModel> {

    infix fun <T : Any> whereExists(where: Where<T>) = where().exists(where)

    fun where(vararg conditions: SQLOperator): Where<TModel> = Where(this, *conditions)

    infix fun where(condition: SQLOperator): Where<TModel> = Where(this, condition)

    override fun cursor(databaseWrapper: DatabaseWrapper): FlowCursor? =
        where().cursor(databaseWrapper)

    override fun groupBy(vararg nameAliases: NameAlias): Where<TModel> =
        where().groupBy(*nameAliases)

    override fun groupBy(vararg properties: IProperty<*>): Where<TModel> =
        where().groupBy(*properties)

    override fun orderBy(nameAlias: NameAlias, ascending: Boolean): Where<TModel> =
        where().orderBy(nameAlias, ascending)

    override fun orderBy(property: IProperty<*>, ascending: Boolean): Where<TModel> =
        where().orderBy(property, ascending)

    override fun orderByAll(orderByList: List<OrderBy>): Where<TModel> =
        where().orderByAll(orderByList)

    override fun orderBy(orderBy: OrderBy): Where<TModel> = where().orderBy(orderBy)

    override fun limit(count: Long): Where<TModel> = where().limit(count)

    override fun offset(offset: Long): Where<TModel> = where().offset(offset)

    override fun having(vararg conditions: SQLOperator): Where<TModel> = where().having(*conditions)

    abstract override fun cloneSelf(): BaseTransformable<TModel>

    override fun queryList(databaseWrapper: DatabaseWrapper): MutableList<TModel> {
        checkSelect("query")
        return super.queryList(databaseWrapper)
    }

    override fun querySingle(databaseWrapper: DatabaseWrapper): TModel? {
        checkSelect("query")
        limit(1)
        return super.querySingle(databaseWrapper)
    }

    private fun checkSelect(methodName: String) {
        if (queryBuilderBase !is Select) {
            throw IllegalArgumentException("Please use $methodName(). The beginning is not a Select")
        }
    }
}
