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

infix fun IConditional.eq(value: IConditional): Operator<*> = this.eq(value)

infix fun IConditional.`is`(conditional: IConditional): Operator<*> = this.`is`(conditional)

infix fun IConditional.isNot(conditional: IConditional): Operator<*> = this.isNot(conditional)

infix fun IConditional.notEq(conditional: IConditional): Operator<*> = this.notEq(conditional)

infix fun IConditional.like(conditional: IConditional): Operator<*> = this.like(conditional)

infix fun IConditional.glob(conditional: IConditional): Operator<*> = this.glob(conditional)

infix fun IConditional.like(value: String): Operator<*> = this.like(value)

infix fun IConditional.glob(value: String): Operator<*> = this.glob(value)

infix fun IConditional.greaterThan(conditional: IConditional): Operator<*> = this.greaterThan(conditional)

infix fun IConditional.greaterThanOrEq(conditional: IConditional): Operator<*> = this.greaterThanOrEq(conditional)

infix fun IConditional.lessThan(conditional: IConditional): Operator<*> = this.lessThan(conditional)

infix fun IConditional.lessThanOrEq(conditional: IConditional): Operator<*> = this.lessThanOrEq(conditional)

infix fun IConditional.between(conditional: IConditional): Operator.Between<*> = this.between(conditional)

infix fun IConditional.`in`(values: Array<IConditional>): Operator.In<*> {
    return when (values.size) {
        1 -> `in`(values[0])
        else -> this.`in`(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun IConditional.notIn(values: Array<IConditional>): Operator.In<*> {
    return when (values.size) {
        1 -> notIn(values[0])
        else -> this.notIn(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun <T : Any> IConditional.`is`(baseModelQueriable: BaseModelQueriable<T>): Operator<*> = this.`is`(baseModelQueriable)

infix fun <T : Any> IConditional.eq(baseModelQueriable: BaseModelQueriable<T>): Operator<*> = this.eq(baseModelQueriable)

infix fun <T : Any> IConditional.isNot(baseModelQueriable: BaseModelQueriable<T>): Operator<*> = this.isNot(baseModelQueriable)
infix fun <T : Any> IConditional.notEq(baseModelQueriable: BaseModelQueriable<T>): Operator<*> = this.notEq(baseModelQueriable)
infix fun <T : Any> IConditional.like(baseModelQueriable: BaseModelQueriable<T>): Operator<*> = this.like(baseModelQueriable)
infix fun <T : Any> IConditional.glob(baseModelQueriable: BaseModelQueriable<T>): Operator<*> = this.glob(baseModelQueriable)
infix fun <T : Any> IConditional.greaterThan(baseModelQueriable: BaseModelQueriable<T>): Operator<*> = this.greaterThan(baseModelQueriable)
infix fun <T : Any> IConditional.greaterThanOrEq(baseModelQueriable: BaseModelQueriable<T>): Operator<*> = this.greaterThanOrEq(baseModelQueriable)
infix fun <T : Any> IConditional.lessThan(baseModelQueriable: BaseModelQueriable<T>): Operator<*> = this.lessThan(baseModelQueriable)
infix fun <T : Any> IConditional.lessThanOrEq(baseModelQueriable: BaseModelQueriable<T>): Operator<*> = this.lessThanOrEq(baseModelQueriable)
infix fun <T : Any> IConditional.between(baseModelQueriable: BaseModelQueriable<T>): Operator.Between<*> = this.between(baseModelQueriable)

infix fun <T : Any> IConditional.`in`(values: Array<BaseModelQueriable<T>>): Operator.In<*> {
    return when (values.size) {
        1 -> `in`(values[0])
        else -> this.`in`(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun <T : Any> IConditional.notIn(values: Array<BaseModelQueriable<T>>): Operator.In<*> {
    return when (values.size) {
        1 -> notIn(values[0])
        else -> this.notIn(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun IConditional.concatenate(conditional: IConditional): Operator<*> = this.concatenate(conditional)

