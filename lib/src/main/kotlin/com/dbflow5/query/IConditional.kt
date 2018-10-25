package com.dbflow5.query

import com.dbflow5.sql.Query

/**
 * Description: Simple interface for objects that can be used as [Operator]. This class
 * takes no type parameters for primitive objects.
 */
interface IConditional : Query {

    fun isNull(): Operator<*>

    fun isNotNull(): Operator<*>

    infix fun `is`(conditional: IConditional): Operator<*>

    infix fun `is`(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    infix fun eq(conditional: IConditional): Operator<*>

    infix fun eq(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    infix fun concatenate(conditional: IConditional): Operator<*>

    infix fun isNot(conditional: IConditional): Operator<*>

    infix fun isNot(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    infix fun notEq(conditional: IConditional): Operator<*>

    infix fun notEq(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    infix fun like(conditional: IConditional): Operator<*>

    infix fun like(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    infix fun notLike(conditional: IConditional): Operator<*>

    infix fun notLike(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    infix fun glob(conditional: IConditional): Operator<*>

    infix fun glob(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    infix fun like(value: String): Operator<*>

    infix fun notLike(value: String): Operator<*>

    infix fun glob(value: String): Operator<*>

    infix fun greaterThan(conditional: IConditional): Operator<*>

    infix fun greaterThan(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    infix fun greaterThanOrEq(conditional: IConditional): Operator<*>

    infix fun greaterThanOrEq(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    infix fun lessThan(conditional: IConditional): Operator<*>

    infix fun lessThan(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    infix fun lessThanOrEq(conditional: IConditional): Operator<*>

    infix fun lessThanOrEq(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    infix fun between(conditional: IConditional): Operator.Between<*>

    infix fun between(baseModelQueriable: BaseModelQueriable<*>): Operator.Between<*>

    fun `in`(firstConditional: IConditional, vararg conditionals: IConditional): Operator.In<*>

    fun `in`(firstBaseModelQueriable: BaseModelQueriable<*>,
             vararg baseModelQueriables: BaseModelQueriable<*>): Operator.In<*>

    fun notIn(firstConditional: IConditional, vararg conditionals: IConditional): Operator.In<*>

    fun notIn(firstBaseModelQueriable: BaseModelQueriable<*>,
              vararg baseModelQueriables: BaseModelQueriable<*>): Operator.In<*>

    infix operator fun plus(value: BaseModelQueriable<*>): Operator<*>

    infix operator fun minus(value: BaseModelQueriable<*>): Operator<*>

    infix operator fun div(value: BaseModelQueriable<*>): Operator<*>

    infix operator fun times(value: BaseModelQueriable<*>): Operator<*>

    infix operator fun rem(value: BaseModelQueriable<*>): Operator<*>

    infix fun `in`(values: Array<IConditional>): Operator.In<*> {
        return when (values.size) {
            1 -> `in`(values[0])
            else -> this.`in`(values[0], *values.sliceArray(IntRange(1, values.size)))
        }
    }

    infix fun notIn(values: Array<IConditional>): Operator.In<*> {
        return when (values.size) {
            1 -> notIn(values[0])
            else -> this.notIn(values[0], *values.sliceArray(IntRange(1, values.size)))
        }
    }

    infix fun <T : Any> `in`(values: Array<BaseModelQueriable<T>>): Operator.In<*> {
        return when (values.size) {
            1 -> `in`(values[0])
            else -> this.`in`(values[0], *values.sliceArray(IntRange(1, values.size)))
        }
    }

    infix fun <T : Any> notIn(values: Array<BaseModelQueriable<T>>): Operator.In<*> {
        return when (values.size) {
            1 -> notIn(values[0])
            else -> this.notIn(values[0], *values.sliceArray(IntRange(1, values.size)))
        }
    }
}

