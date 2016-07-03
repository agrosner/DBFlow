package com.raizlabs.android.dbflow.kotlinextensions

import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable
import com.raizlabs.android.dbflow.sql.language.Condition
import com.raizlabs.android.dbflow.sql.language.IConditional
import com.raizlabs.android.dbflow.sql.language.property.Property
import com.raizlabs.android.dbflow.structure.Model

/**
 * Description: Provides property methods in via infix functions.
 */

infix fun <TModel> Property<TModel>.eq(value: TModel) = this.eq(value)

infix fun <TModel> Property<TModel>.`is`(value: TModel) = this.`is`(value)

infix fun <TModel> Property<TModel>.isNot(value: TModel) = this.isNot(value)

infix fun <TModel> Property<TModel>.notEq(value: TModel) = this.notEq(value)

infix fun <TModel> Property<TModel>.like(value: String) = this.like(value)

infix fun <TModel> Property<TModel>.glob(value: String) = this.glob(value)

infix fun <TModel> Property<TModel>.greaterThan(value: TModel) = this.greaterThan(value)

infix fun <TModel> Property<TModel>.greaterThanOrEq(value: TModel) = this.greaterThanOrEq(value)

infix fun <TModel> Property<TModel>.lessThan(value: TModel) = this.lessThan(value)

infix fun <TModel> Property<TModel>.lessThanOrEq(value: TModel) = this.lessThanOrEq(value)

infix fun <TModel> Property<TModel>.between(value: TModel) = this.between(value)

infix fun <TModel> Property<TModel>.`in`(values: Array<TModel>): Condition.In {
    return when (values.size) {
        1 -> `in`(values[0])
        else -> this.`in`(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun <TModel> Property<TModel>.notIn(values: Array<TModel>): Condition.In {
    return when (values.size) {
        1 -> notIn(values[0])
        else -> this.notIn(values[0], *values.sliceArray(IntRange(1, values.size)))
    }
}

infix fun <TModel> Property<TModel>.`in`(values: Collection<TModel>) = this.`in`(values)

infix fun <TModel> Property<TModel>.notIn(values: Collection<TModel>) = this.notIn(values)

infix fun <TModel> Property<TModel>.concatenate(value: TModel) = this.concatenate(value)

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

