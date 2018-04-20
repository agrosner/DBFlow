package com.raizlabs.dbflow5.query.property

import com.raizlabs.dbflow5.JvmStatic
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.query.*
import kotlin.reflect.KClass

expect open class Property<T> : InternalProperty<T> {

    constructor(nameAlias: NameAlias)

    constructor(table: KClass<*>?, nameAlias: NameAlias)

    constructor(table: KClass<*>?, columnName: String?)

    constructor(table: KClass<*>?, columnName: String, aliasName: String)

    companion object {
        @JvmStatic
        val ALL_PROPERTY: Property<String>

        @JvmStatic
        val WILDCARD: Property<*>

        @JvmStatic
        fun allProperty(table: KClass<*>): Property<String>
    }
}

/**
 * Description: The main, immutable property class that gets generated from a table definition.
 *
 *
 * This class delegates all of its [IOperator] methods to a new [Operator] that's used
 * in the SQLite query language.
 *
 *
 * This ensures that the language is strictly type-safe and only declared
 * columns get used. Also any calls on the methods return a new [Property].
 *
 *
 * This is type parametrized so that all values passed to this class remain properly typed.
 */
open class InternalProperty<T> : IProperty<Property<T>>, IConditional, IOperator<T> {

    final override val table: KClass<*>?

    private lateinit var internalNameAlias: NameAlias

    val definition: String
        get() = nameAlias.fullQuery

    /**
     * @return helper method to construct it in a [.distinct] call.
     */
    private val distinctAliasName: NameAlias
        get() = nameAlias
            .newBuilder()
            .distinct()
            .build()

    protected open val operator: Operator<T>
        get() = Operator.op(nameAlias)

    constructor(nameAlias: NameAlias) : this(null, nameAlias)

    constructor(table: KClass<*>?, nameAlias: NameAlias) {
        this.table = table
        this.internalNameAlias = nameAlias
    }

    constructor(table: KClass<*>?, columnName: String?) {
        this.table = table
        if (columnName != null) {
            internalNameAlias = NameAlias.Builder(columnName).build()
        }
    }

    constructor(table: KClass<*>?, columnName: String, aliasName: String)
        : this(table, NameAlias.builder(columnName).`as`(aliasName).build())

    override fun withTable(): Property<T> = Property(table, nameAlias
        .newBuilder()
        .withTable(FlowManager.getTableName(table!!))
        .build())

    override val nameAlias: NameAlias
        get() = internalNameAlias

    override val query: String
        get() = nameAlias.query

    override val cursorKey: String
        get() = nameAlias.query

    override fun toString(): String = nameAlias.toString()

    override fun `is`(conditional: IConditional): Operator<*> = operator.`is`(conditional)

    override fun eq(conditional: IConditional): Operator<*> = operator.eq(conditional)

    override fun isNot(conditional: IConditional): Operator<*> = operator.isNot(conditional)

    override fun notEq(conditional: IConditional): Operator<*> = operator.notEq(conditional)

    override fun like(conditional: IConditional): Operator<*> = operator.like(conditional)

    override fun glob(conditional: IConditional): Operator<*> = operator.glob(conditional)

    override fun like(value: String): Operator<T> = operator.like(value)

    override fun notLike(value: String): Operator<T> = operator.notLike(value)

    override fun glob(value: String): Operator<T> = operator.glob(value)

    override fun greaterThan(conditional: IConditional): Operator<*> =
        operator.greaterThan(conditional)

    override fun greaterThanOrEq(conditional: IConditional): Operator<*> =
        operator.greaterThanOrEq(conditional)

    override fun lessThan(conditional: IConditional): Operator<*> = operator.lessThan(conditional)

    override fun lessThanOrEq(conditional: IConditional): Operator<*> =
        operator.lessThanOrEq(conditional)

    override fun between(conditional: IConditional): Operator.Between<*> =
        operator.between(conditional)

    override fun `in`(firstConditional: IConditional, vararg conditionals: IConditional): Operator.In<*> =
        operator.`in`(firstConditional, *conditionals)

    override fun notIn(firstConditional: IConditional, vararg conditionals: IConditional): Operator.In<*> =
        operator.notIn(firstConditional, *conditionals)

    override fun `is`(baseModelQueriable: BaseModelQueriable<*>): Operator<*> =
        operator.`is`(baseModelQueriable)

    override fun isNull(): Operator<*> = operator.isNull()

    override fun eq(baseModelQueriable: BaseModelQueriable<*>): Operator<*> =
        operator.eq(baseModelQueriable)

    override fun isNot(baseModelQueriable: BaseModelQueriable<*>): Operator<*> =
        operator.isNot(baseModelQueriable)

    override fun isNotNull(): Operator<*> = operator.isNotNull()

    override fun notEq(baseModelQueriable: BaseModelQueriable<*>): Operator<*> =
        operator.notEq(baseModelQueriable)

    override fun like(baseModelQueriable: BaseModelQueriable<*>): Operator<*> =
        operator.like(baseModelQueriable)

    override fun notLike(conditional: IConditional): Operator<*> = operator.notLike(conditional)

    override fun notLike(baseModelQueriable: BaseModelQueriable<*>): Operator<*> =
        operator.notLike(baseModelQueriable)

    override fun glob(baseModelQueriable: BaseModelQueriable<*>): Operator<*> =
        operator.glob(baseModelQueriable)

    override fun greaterThan(baseModelQueriable: BaseModelQueriable<*>): Operator<*> =
        operator.greaterThan(baseModelQueriable)

    override fun greaterThanOrEq(baseModelQueriable: BaseModelQueriable<*>): Operator<*> =
        operator.greaterThanOrEq(baseModelQueriable)

    override fun lessThan(baseModelQueriable: BaseModelQueriable<*>): Operator<*> =
        operator.lessThan(baseModelQueriable)

    override fun lessThanOrEq(baseModelQueriable: BaseModelQueriable<*>): Operator<*> =
        operator.lessThanOrEq(baseModelQueriable)

    override fun between(baseModelQueriable: BaseModelQueriable<*>): Operator.Between<*> =
        operator.between(baseModelQueriable)

    override fun `in`(firstBaseModelQueriable: BaseModelQueriable<*>,
                      vararg baseModelQueriables: BaseModelQueriable<*>): Operator.In<*> =
        operator.`in`(firstBaseModelQueriable, *baseModelQueriables)

    override fun notIn(firstBaseModelQueriable: BaseModelQueriable<*>,
                       vararg baseModelQueriables: BaseModelQueriable<*>): Operator.In<*> =
        operator.notIn(firstBaseModelQueriable, *baseModelQueriables)

    override fun concatenate(conditional: IConditional): Operator<*> =
        operator.concatenate(conditional)

    override fun plus(value: BaseModelQueriable<*>): Operator<*> = operator.plus(value)

    override fun minus(value: BaseModelQueriable<*>): Operator<*> = operator.minus(value)

    override fun div(value: BaseModelQueriable<*>): Operator<*> = operator.div(value)

    override fun times(value: BaseModelQueriable<*>): Operator<*> = operator.times(value)

    override fun rem(value: BaseModelQueriable<*>): Operator<*> = operator.rem(value)

    override fun plus(property: IProperty<*>): Property<T> {
        return Property(table, NameAlias.joinNames(Operator.Operation.PLUS,
            internalNameAlias.fullName(), property.toString()))
    }

    override fun minus(property: IProperty<*>): Property<T> {
        return Property(table, NameAlias.joinNames(Operator.Operation.MINUS,
            internalNameAlias.fullName(), property.toString()))
    }

    override fun div(property: IProperty<*>): Property<T> {
        return Property(table, NameAlias.joinNames(Operator.Operation.DIVISION,
            internalNameAlias.fullName(), property.toString()))
    }

    override fun times(property: IProperty<*>): Property<T> {
        return Property(table, NameAlias.joinNames(Operator.Operation.MULTIPLY,
            internalNameAlias.fullName(), property.toString()))
    }

    override fun rem(property: IProperty<*>): Property<T> {
        return Property(table, NameAlias.joinNames(Operator.Operation.MOD,
            internalNameAlias.fullName(), property.toString()))
    }

    override fun concatenate(property: IProperty<*>): Property<T> {
        return Property(table, NameAlias.joinNames(Operator.Operation.CONCATENATE,
            internalNameAlias.fullName(), property.toString()))
    }

    override fun `as`(aliasName: String): Property<T> {
        return Property(table, nameAlias
            .newBuilder()
            .`as`(aliasName)
            .build())
    }

    override fun distinct(): Property<T> = Property(table, distinctAliasName)

    override fun withTable(tableNameAlias: NameAlias): Property<T> {
        return Property(table, nameAlias
            .newBuilder()
            .withTable(tableNameAlias.tableName)
            .build())
    }

    override fun `is`(value: T?): Operator<T> = operator.`is`(value)

    override fun eq(value: T?): Operator<T> = operator.eq(value)

    override fun isNot(value: T?): Operator<T> = operator.isNot(value)

    override fun notEq(value: T?): Operator<T> = operator.notEq(value)

    override fun greaterThan(value: T): Operator<T> = operator.greaterThan(value)

    override fun greaterThanOrEq(value: T): Operator<T> = operator.greaterThanOrEq(value)

    override fun lessThan(value: T): Operator<T> = operator.lessThan(value)

    override fun lessThanOrEq(value: T): Operator<T> = operator.lessThanOrEq(value)

    override fun between(value: T): Operator.Between<T> = operator.between(value)

    override fun `in`(firstValue: T, vararg values: T): Operator.In<T> =
        operator.`in`(firstValue, *values)

    override fun notIn(firstValue: T, vararg values: T): Operator.In<T> =
        operator.notIn(firstValue, *values)

    override fun `in`(values: Collection<T>): Operator.In<T> = operator.`in`(values)

    override fun notIn(values: Collection<T>): Operator.In<T> = operator.notIn(values)

    override fun concatenate(value: Any?): Operator<T> = operator.concatenate(value)

    override fun plus(value: T): Operator<T> = operator.plus(value)

    override fun minus(value: T): Operator<T> = operator.minus(value)

    override fun div(value: T): Operator<T> = operator.div(value)

    override fun times(value: T): Operator<T> = operator.times(value)

    override fun rem(value: T): Operator<T> = operator.rem(value)

    override fun asc(): OrderBy = OrderBy.fromProperty(this).ascending()

    override fun desc(): OrderBy = OrderBy.fromProperty(this).descending()

    companion object {

        @JvmStatic
        val ALL_PROPERTY = Property<String>(NameAlias.rawBuilder("*").build())

        @JvmStatic
        val WILDCARD: Property<*> = Property<Any>(NameAlias.rawBuilder("?").build())

        @JvmStatic
        fun allProperty(table: KClass<*>): Property<String> =
            Property<String>(table, NameAlias.rawBuilder("*").build()).withTable()
    }
}


