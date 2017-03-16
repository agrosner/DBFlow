package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable
import com.raizlabs.android.dbflow.sql.language.IConditional
import com.raizlabs.android.dbflow.sql.language.Operator
import com.raizlabs.android.dbflow.sql.language.property.Property
import com.raizlabs.android.dbflow.structure.Model

/**
 * Description: Provides property methods in via infix functions.
 */

infix fun <T : Any> Property<T>.eq(value: T): Operator<T> = this.eq(value)

infix fun <T : Any> Property<T>.`is`(value: T): Operator<T> = this.`is`(value)

infix fun <T : Any> Property<T>.isNot(value: T): Operator<T> = this.isNot(value)

infix fun <T : Any> Property<T>.notEq(value: T): Operator<T> = this.notEq(value)

infix fun <T : Any> Property<T>.like(value: String): Operator<T> = this.like(value)

infix fun <T : Any> Property<T>.glob(value: String): Operator<T> = this.glob(value)

infix fun <T : Any> Property<T>.greaterThan(value: T): Operator<T> = this.greaterThan(value)

infix fun <T : Any> Property<T>.greaterThanOrEq(value: T): Operator<T> = this.greaterThanOrEq(value)

infix fun <T : Any> Property<T>.lessThan(value: T): Operator<T> = this.lessThan(value)

infix fun <T : Any> Property<T>.lessThanOrEq(value: T): Operator<T> = this.lessThanOrEq(value)

infix fun <T : Any> Property<T>.between(value: T): Operator.Between<T> = this.between(value)

infix fun <T : Any> Property<T>.`in`(values: Array<T>): Operator.In<T> {
    return when (values.size) {
        1 -> `in`(values[0])
        else -> this.`in`(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun <T : Any> Property<T>.notIn(values: Array<T>): Operator.In<T> {
    return when (values.size) {
        1 -> notIn(values[0])
        else -> this.notIn(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun <T : Any> Property<T>.`in`(values: Collection<T>): Operator.In<T> = this.`in`(values)

infix fun <T : Any> Property<T>.notIn(values: Collection<T>): Operator.In<T> = this.notIn(values)

infix fun <T : Any> Property<T>.concatenate(value: T): Operator<T> = this.concatenate(value)

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

infix fun IConditional.`is`(baseModelQueriable: BaseModelQueriable<Model>): Operator<*> = this.`is`(baseModelQueriable)

infix fun IConditional.eq(baseModelQueriable: BaseModelQueriable<Model>): Operator<*> = this.eq(baseModelQueriable)

infix fun IConditional.isNot(baseModelQueriable: BaseModelQueriable<Model>): Operator<*> = this.isNot(baseModelQueriable)

infix fun IConditional.notEq(baseModelQueriable: BaseModelQueriable<Model>): Operator<*> = this.notEq(baseModelQueriable)

infix fun IConditional.like(baseModelQueriable: BaseModelQueriable<Model>): Operator<*> = this.like(baseModelQueriable)

infix fun IConditional.glob(baseModelQueriable: BaseModelQueriable<Model>): Operator<*> = this.glob(baseModelQueriable)

infix fun IConditional.greaterThan(baseModelQueriable: BaseModelQueriable<Model>): Operator<*> = this.greaterThan(baseModelQueriable)

infix fun IConditional.greaterThanOrEq(baseModelQueriable: BaseModelQueriable<Model>): Operator<*> = this.greaterThanOrEq(baseModelQueriable)

infix fun IConditional.lessThan(baseModelQueriable: BaseModelQueriable<Model>): Operator<*> = this.lessThan(baseModelQueriable)

infix fun IConditional.lessThanOrEq(baseModelQueriable: BaseModelQueriable<Model>): Operator<*> = this.lessThanOrEq(baseModelQueriable)

infix fun IConditional.between(baseModelQueriable: BaseModelQueriable<Model>): Operator.Between<*> = this.between(baseModelQueriable)

infix fun IConditional.`in`(values: Array<BaseModelQueriable<Model>>): Operator.In<*> {
    return when (values.size) {
        1 -> `in`(values[0])
        else -> this.`in`(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun IConditional.notIn(values: Array<BaseModelQueriable<Model>>): Operator.In<*> {
    return when (values.size) {
        1 -> notIn(values[0])
        else -> this.notIn(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun IConditional.concatenate(conditional: IConditional): Operator<*> = this.concatenate(conditional)

