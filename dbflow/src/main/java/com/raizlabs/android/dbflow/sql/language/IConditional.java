package com.raizlabs.android.dbflow.sql.language;

/**
 * Description: Interface for objects that can be used as {@link Condition}
 */
public interface IConditional<ValueType> {

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
}
