package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;

/**
 * Description: Interface for objects that can be used as {@link Condition}
 */
public interface IConditional<ValueType> extends Query {

    Condition is(ValueType value);

    Condition eq(ValueType value);

    Condition isNot(ValueType value);

    Condition notEq(ValueType value);

    Condition like(ValueType value);

    Condition glob(ValueType value);

    Condition greaterThan(ValueType value);

    Condition greaterThanOrEq(ValueType value);

    Condition lessThan(ValueType value);

    Condition lessThanOrEq(ValueType value);

    Condition.Between between(ValueType value);

    Condition.In in(ValueType firstValue, ValueType... values);

    Condition.In notIn(ValueType firstValue, ValueType... values);

    Condition is(IConditional<ValueType> conditional);

    Condition eq(IConditional<ValueType> conditional);

    Condition isNot(IConditional<ValueType> conditional);

    Condition notEq(IConditional<ValueType> conditional);

    Condition like(IConditional<ValueType> conditional);

    Condition glob(IConditional<ValueType> conditional);

    Condition greaterThan(IConditional<ValueType> conditional);

    Condition greaterThanOrEq(IConditional<ValueType> conditional);

    Condition lessThan(IConditional<ValueType> conditional);

    Condition lessThanOrEq(IConditional<ValueType> conditional);

    Condition.Between between(IConditional<ValueType> conditional);

    Condition.In in(IConditional<ValueType> firstConditional, IConditional<ValueType>... conditionals);

    Condition.In notIn(IConditional<ValueType> firstConditional, IConditional<ValueType>... conditionals);
}
