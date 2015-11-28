package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;

/**
 * Description: Interface for objects that can be used as {@link Condition} that have object types.
 */
public interface ITypeConditional<ValueType> extends Query, IConditional {

    Condition is(ValueType value);

    Condition eq(ValueType value);

    Condition concatenate(ValueType value);

    Condition isNot(ValueType value);

    Condition notEq(ValueType value);

    Condition like(String value);

    Condition glob(String value);

    Condition greaterThan(ValueType value);

    Condition greaterThanOrEq(ValueType value);

    Condition lessThan(ValueType value);

    Condition lessThanOrEq(ValueType value);

    Condition.Between between(ValueType value);

    Condition.In in(ValueType firstValue, ValueType... values);

    Condition.In notIn(ValueType firstValue, ValueType... values);

}
