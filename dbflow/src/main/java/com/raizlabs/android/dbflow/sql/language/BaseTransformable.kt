package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.sql.language.property.IProperty
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.FlowCursor

/**
 * Description: Combines basic transformations and query ops into a base class.
 */
abstract class BaseTransformable<TModel : Any>
/**
 * Constructs new instance of this class and is meant for subclasses only.
 *
 * @param table the table that belongs to this query.
 */
protected constructor(databaseWrapper: DatabaseWrapper,
                      table: Class<TModel>)
    : BaseModelQueriable<TModel>(databaseWrapper, table), Transformable<TModel>, WhereBase<TModel> {

    infix fun <T : Any> whereExists(where: Where<T>) = where().exists(where)

    fun where(vararg conditions: SQLOperator): Where<TModel> = Where(this, *conditions)

    override fun query(): FlowCursor? = where().query()

    override fun groupBy(vararg nameAliases: NameAlias): Where<TModel> =
            where().groupBy(*nameAliases)

    override fun groupBy(vararg properties: IProperty<*>): Where<TModel> =
            where().groupBy(*properties)

    override fun orderBy(nameAlias: NameAlias, ascending: Boolean): Where<TModel> =
            where().orderBy(nameAlias, ascending)

    override fun orderBy(property: IProperty<*>, ascending: Boolean): Where<TModel> =
            where().orderBy(property, ascending)

    override fun orderByAll(orderBies: List<OrderBy>): Where<TModel> = where().orderByAll(orderBies)

    override fun orderBy(orderBy: OrderBy): Where<TModel> = where().orderBy(orderBy)

    override fun limit(count: Int): Where<TModel> = where().limit(count)

    override fun offset(offset: Int): Where<TModel> = where().offset(offset)

    override fun having(vararg conditions: SQLOperator): Where<TModel> = where().having(*conditions)

    override fun queryList(): MutableList<TModel> {
        checkSelect("query")
        return super.queryList()
    }

    override fun querySingle(): TModel? {
        checkSelect("query")
        limit(1)
        return super.querySingle()
    }

    private fun checkSelect(methodName: String) {
        if (queryBuilderBase !is Select) {
            throw IllegalArgumentException("Please use $methodName(). The beginning is not a Select")
        }
    }
}

infix fun <T : Any> BaseTransformable<T>.where(sqlOperator: SQLOperator) = where(sqlOperator)
