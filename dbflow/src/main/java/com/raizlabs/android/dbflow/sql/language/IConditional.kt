package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.sql.Query

/**
 * Description: Simple interface for objects that can be used as [Operator]. This class
 * takes no type parameters for primitive objects.
 */
interface IConditional : Query {

    fun isNull(): Operator<*>

    fun isNotNull(): Operator<*>

    fun `is`(conditional: IConditional): Operator<*>

    fun `is`(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    fun eq(conditional: IConditional): Operator<*>

    fun eq(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    fun concatenate(conditional: IConditional): Operator<*>

    fun isNot(conditional: IConditional): Operator<*>

    fun isNot(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    fun notEq(conditional: IConditional): Operator<*>

    fun notEq(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    fun like(conditional: IConditional): Operator<*>

    fun like(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    fun notLike(conditional: IConditional): Operator<*>

    fun notLike(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    fun glob(conditional: IConditional): Operator<*>

    fun glob(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    fun like(value: String): Operator<*>

    fun notLike(value: String): Operator<*>

    fun glob(value: String): Operator<*>

    fun greaterThan(conditional: IConditional): Operator<*>

    fun greaterThan(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    fun greaterThanOrEq(conditional: IConditional): Operator<*>

    fun greaterThanOrEq(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    fun lessThan(conditional: IConditional): Operator<*>

    fun lessThan(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    fun lessThanOrEq(conditional: IConditional): Operator<*>

    fun lessThanOrEq(baseModelQueriable: BaseModelQueriable<*>): Operator<*>

    fun between(conditional: IConditional): Operator.Between<*>

    fun between(baseModelQueriable: BaseModelQueriable<*>): Operator.Between<*>

    fun `in`(firstConditional: IConditional, vararg conditionals: IConditional): Operator.In<*>

    fun `in`(firstBaseModelQueriable: BaseModelQueriable<*>,
             vararg baseModelQueriables: BaseModelQueriable<*>): Operator.In<*>

    fun notIn(firstConditional: IConditional, vararg conditionals: IConditional): Operator.In<*>

    fun notIn(firstBaseModelQueriable: BaseModelQueriable<*>,
              vararg baseModelQueriables: BaseModelQueriable<*>): Operator.In<*>

    operator fun plus(value: BaseModelQueriable<*>): Operator<*>

    operator fun minus(value: BaseModelQueriable<*>): Operator<*>

    operator fun div(value: BaseModelQueriable<*>): Operator<*>

    operator fun times(value: BaseModelQueriable<*>): Operator<*>

    operator fun rem(value: BaseModelQueriable<*>): Operator<*>
}
