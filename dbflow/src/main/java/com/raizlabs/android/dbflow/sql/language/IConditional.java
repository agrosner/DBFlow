package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;

/**
 * Description: Interface for objects that can be used as {@link Condition}
 */
public interface IConditional<ValueType> extends Query {

    Condition is(ValueType value);

    Condition is(IConditional conditional);

    Condition is(BaseModelQueriable baseModelQueriable);

    Condition isNull();

    Condition eq(ValueType value);

    Condition eq(IConditional conditional);

    Condition eq(BaseModelQueriable baseModelQueriable);

    Condition concatenate(ValueType value);

    Condition concatenate(IConditional conditional);

    Condition isNot(ValueType value);

    Condition isNot(IConditional conditional);

    Condition isNot(BaseModelQueriable baseModelQueriable);

    Condition isNotNull();

    Condition notEq(ValueType value);

    Condition notEq(IConditional<ValueType> conditional);

    Condition notEq(BaseModelQueriable baseModelQueriable);

    Condition like(ValueType value);

    Condition like(IConditional conditional);

    Condition like(BaseModelQueriable baseModelQueriable);

    Condition glob(ValueType value);

    Condition glob(IConditional conditional);

    Condition glob(BaseModelQueriable baseModelQueriable);

    Condition greaterThan(ValueType value);

    Condition greaterThan(IConditional conditional);

    Condition greaterThan(BaseModelQueriable baseModelQueriable);

    Condition greaterThanOrEq(ValueType value);

    Condition greaterThanOrEq(IConditional conditional);

    Condition greaterThanOrEq(BaseModelQueriable baseModelQueriable);

    Condition lessThan(ValueType value);

    Condition lessThan(IConditional conditional);

    Condition lessThan(BaseModelQueriable baseModelQueriable);

    Condition lessThanOrEq(ValueType value);

    Condition lessThanOrEq(IConditional conditional);

    Condition lessThanOrEq(BaseModelQueriable baseModelQueriable);

    Condition.Between between(ValueType value);

    Condition.Between between(IConditional conditional);

    Condition.Between between(BaseModelQueriable baseModelQueriable);

    Condition.In in(ValueType firstValue, ValueType... values);

    Condition.In in(IConditional firstConditional, IConditional... conditionals);

    Condition.In in(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables);

    Condition.In notIn(ValueType firstValue, ValueType... values);

    Condition.In notIn(IConditional firstConditional, IConditional... conditionals);

    Condition.In notIn(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable... baseModelQueriables);
}
