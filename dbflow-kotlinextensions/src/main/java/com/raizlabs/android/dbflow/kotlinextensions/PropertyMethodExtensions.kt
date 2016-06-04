package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable
import com.raizlabs.android.dbflow.sql.language.Condition
import com.raizlabs.android.dbflow.sql.language.IConditional
import com.raizlabs.android.dbflow.sql.language.property.BaseProperty
import com.raizlabs.android.dbflow.sql.language.property.IProperty
import com.raizlabs.android.dbflow.sql.language.property.Property
import com.raizlabs.android.dbflow.structure.Model

/**
 * Description: Provides property methods in via infix functions.
 */

infix fun <T> Property<T>.eq(value: T): Condition = this.eq(value)

infix fun <T> Property<T>.`is`(value: T): Condition = this.`is`(value)

infix fun <T> Property<T>.isNot(value: T): Condition = this.isNot(value)

infix fun <T> Property<T>.notEq(value: T): Condition = this.notEq(value)

infix fun <T> Property<T>.like(value: String): Condition = this.like(value)

infix fun <T> Property<T>.glob(value: String): Condition = this.glob(value)

infix fun <T> Property<T>.greaterThan(value: T): Condition = this.greaterThan(value)

infix fun <T> Property<T>.greaterThanOrEq(value: T): Condition = this.greaterThanOrEq(value)

infix fun <T> Property<T>.lessThan(value: T): Condition = this.lessThan(value)

infix fun <T> Property<T>.lessThanOrEq(value: T): Condition = this.lessThanOrEq(value)

infix fun <T> Property<T>.between(value: T): Condition.Between = this.between(value)

infix fun <T> Property<T>.`in`(values: Array<T>): Condition.In {
    return when (values.size) {
        1 -> `in`(values[0])
        else -> this.`in`(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun <T> Property<T>.notIn(values: Array<T>): Condition.In {
    return when (values.size) {
        1 -> notIn(values[0])
        else -> this.notIn(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun <T> Property<T>.`in`(values: Collection<T>): Condition.In = this.`in`(values)

infix fun <T> Property<T>.notIn(values: Collection<T>): Condition.In = this.notIn(values)

infix fun <T> Property<T>.concatenate(value: T): Condition = this.concatenate(value)

infix fun IConditional.eq(value: IConditional): Condition = this.eq(value)

infix fun IConditional.`is`(conditional: IConditional): Condition {
    return this.`is`(conditional)
}

infix fun IConditional.isNot(conditional: IConditional): Condition {
    return this.isNot(conditional)
}

infix fun IConditional.notEq(conditional: IConditional): Condition {
    return this.notEq(conditional)
}

infix fun IConditional.like(conditional: IConditional): Condition {
    return this.like(conditional)
}

infix fun IConditional.glob(conditional: IConditional): Condition {
    return this.glob(conditional)
}

infix fun IConditional.like(value: String): Condition {
    return this.like(value)
}

infix fun IConditional.glob(value: String): Condition {
    return this.glob(value)
}

infix fun IConditional.greaterThan(conditional: IConditional): Condition {
    return this.greaterThan(conditional)
}

infix fun IConditional.greaterThanOrEq(conditional: IConditional): Condition {
    return this.greaterThanOrEq(conditional)
}

infix fun IConditional.lessThan(conditional: IConditional): Condition {
    return this.lessThan(conditional)
}

infix fun IConditional.lessThanOrEq(conditional: IConditional): Condition {
    return this.lessThanOrEq(conditional)
}

infix fun IConditional.between(conditional: IConditional): Condition.Between {
    return this.between(conditional)
}

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

infix fun IConditional.`is`(baseModelQueriable: BaseModelQueriable<Model>): Condition {
    return this.`is`(baseModelQueriable)
}

infix fun IConditional.eq(baseModelQueriable: BaseModelQueriable<Model>): Condition {
    return this.eq(baseModelQueriable)
}

infix fun IConditional.isNot(baseModelQueriable: BaseModelQueriable<Model>): Condition {
    return this.isNot(baseModelQueriable)
}

infix fun IConditional.notEq(baseModelQueriable: BaseModelQueriable<Model>): Condition {
    return this.notEq(baseModelQueriable)
}

infix fun IConditional.like(baseModelQueriable: BaseModelQueriable<Model>): Condition {
    return this.like(baseModelQueriable)
}

infix fun IConditional.glob(baseModelQueriable: BaseModelQueriable<Model>): Condition {
    return this.glob(baseModelQueriable)
}

infix fun IConditional.greaterThan(baseModelQueriable: BaseModelQueriable<Model>): Condition {
    return this.greaterThan(baseModelQueriable)
}

infix fun IConditional.greaterThanOrEq(baseModelQueriable: BaseModelQueriable<Model>): Condition {
    return this.greaterThanOrEq(baseModelQueriable)
}

infix fun IConditional.lessThan(baseModelQueriable: BaseModelQueriable<Model>): Condition {
    return this.lessThan(baseModelQueriable)
}

infix fun IConditional.lessThanOrEq(baseModelQueriable: BaseModelQueriable<Model>): Condition {
    return this.lessThanOrEq(baseModelQueriable)
}

infix fun IConditional.between(baseModelQueriable: BaseModelQueriable<Model>): Condition.Between {
    return this.between(baseModelQueriable)
}

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

infix fun IConditional.concatenate(conditional: IConditional): Condition {
    return this.concatenate(conditional)
}

