package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable
import com.raizlabs.android.dbflow.sql.language.Condition
import com.raizlabs.android.dbflow.sql.language.IConditional
import com.raizlabs.android.dbflow.sql.language.property.Property
import com.raizlabs.android.dbflow.structure.Model

/**
 * Description: Provides property methods in via infix functions.
 */
infix fun <T : Any> Property<T>.eq(value: T) = this.eq(value)

infix fun <T : Any> Property<T>.`is`(value: T) = this.`is`(value)

infix fun <T : Any> Property<T>.isNot(value: T) = this.isNot(value)

infix fun <T : Any> Property<T>.notEq(value: T) = this.notEq(value)

infix fun <T : Any> Property<T>.like(value: String) = this.like(value)

infix fun <T : Any> Property<T>.glob(value: String) = this.glob(value)

infix fun <T : Any> Property<T>.greaterThan(value: T) = this.greaterThan(value)

infix fun <T : Any> Property<T>.greaterThanOrEq(value: T) = this.greaterThanOrEq(value)

infix fun <T : Any> Property<T>.lessThan(value: T) = this.lessThan(value)

infix fun <T : Any> Property<T>.lessThanOrEq(value: T) = this.lessThanOrEq(value)

infix fun <T : Any> Property<T>.between(value: T) = this.between(value)

infix fun <T : Any> Property<T>.`in`(values: Array<T>): Condition.In {
    return when (values.size) {
        1 -> `in`(values[0])
        else -> this.`in`(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun <T : Any> Property<T>.notIn(values: Array<T>): Condition.In {
    return when (values.size) {
        1 -> notIn(values[0])
        else -> this.notIn(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun <T : Any> Property<T>.`in`(values: Collection<T>) = this.`in`(values)

infix fun <T : Any> Property<T>.notIn(values: Collection<T>) = this.notIn(values)

infix fun <T : Any> Property<T>.concatenate(value: T) = this.concatenate(value)

infix fun IConditional.eq(value: IConditional) = this.eq(value)

infix fun IConditional.`is`(conditional: IConditional) = this.`is`(conditional)

infix fun IConditional.isNot(conditional: IConditional) = this.isNot(conditional)

infix fun IConditional.notEq(conditional: IConditional) = this.notEq(conditional)

infix fun IConditional.like(conditional: IConditional) = this.like(conditional)

infix fun IConditional.glob(conditional: IConditional) = this.glob(conditional)

infix fun IConditional.like(value: String) = this.like(value)

infix fun IConditional.glob(value: String) = this.glob(value)

infix fun IConditional.greaterThan(conditional: IConditional) = this.greaterThan(conditional)

infix fun IConditional.greaterThanOrEq(conditional: IConditional) = this.greaterThanOrEq(conditional)

infix fun IConditional.lessThan(conditional: IConditional) = this.lessThan(conditional)

infix fun IConditional.lessThanOrEq(conditional: IConditional) = this.lessThanOrEq(conditional)

infix fun IConditional.between(conditional: IConditional) = this.between(conditional)

infix fun IConditional.`in`(values: Array<IConditional>): Condition.In {
    return when (values.size) {
        1 -> `in`(values[0])
        else -> this.`in`(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun IConditional.notIn(values: Array<IConditional>): Condition.In {
    return when (values.size) {
        1 -> notIn(values[0])
        else -> this.notIn(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun IConditional.`is`(baseModelQueriable: BaseModelQueriable<Model>) = this.`is`(baseModelQueriable)

infix fun IConditional.eq(baseModelQueriable: BaseModelQueriable<Model>) = this.eq(baseModelQueriable)

infix fun IConditional.isNot(baseModelQueriable: BaseModelQueriable<Model>) = this.isNot(baseModelQueriable)

infix fun IConditional.notEq(baseModelQueriable: BaseModelQueriable<Model>) = this.notEq(baseModelQueriable)

infix fun IConditional.like(baseModelQueriable: BaseModelQueriable<Model>) = this.like(baseModelQueriable)

infix fun IConditional.glob(baseModelQueriable: BaseModelQueriable<Model>) = this.glob(baseModelQueriable)

infix fun IConditional.greaterThan(baseModelQueriable: BaseModelQueriable<Model>) = this.greaterThan(baseModelQueriable)

infix fun IConditional.greaterThanOrEq(baseModelQueriable: BaseModelQueriable<Model>) = this.greaterThanOrEq(baseModelQueriable)

infix fun IConditional.lessThan(baseModelQueriable: BaseModelQueriable<Model>) = this.lessThan(baseModelQueriable)

infix fun IConditional.lessThanOrEq(baseModelQueriable: BaseModelQueriable<Model>) = this.lessThanOrEq(baseModelQueriable)

infix fun IConditional.between(baseModelQueriable: BaseModelQueriable<Model>) = this.between(baseModelQueriable)

infix fun IConditional.`in`(values: Array<BaseModelQueriable<Model>>): Condition.In {
    return when (values.size) {
        1 -> `in`(values[0])
        else -> this.`in`(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun IConditional.notIn(values: Array<BaseModelQueriable<Model>>): Condition.In {
    return when (values.size) {
        1 -> notIn(values[0])
        else -> this.notIn(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun IConditional.concatenate(conditional: IConditional) = this.concatenate(conditional)

