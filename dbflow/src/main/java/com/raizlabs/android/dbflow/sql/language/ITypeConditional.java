package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;

import java.util.Collection;

/**
 * Description: Interface for objects that can be used as {@link Condition} that have a type parameter.
 */
public interface ITypeConditional<ValueType> extends Query, IConditional {

    /**
     * Assigns the operation to "="
     *
     * @param value The {@link ValueType} that we express equality to.
     * @return A {@link Condition} that represents equality between this and the parameter.
     */
    Condition is(ValueType value);

    /**
     * Assigns the operation to "=". Identical to {@link #is(ValueType)}
     *
     * @param value The {@link ValueType} that we express equality to.
     * @return A {@link Condition} that represents equality between this and the parameter.
     * @see #is(ValueType)
     */
    Condition eq(ValueType value);

    /**
     * Generates a {@link Condition} that concatenates this {@link ITypeConditional} with the {@link ValueType} via "||"
     * by columnName=columnName || value
     *
     * @param value The value to concatenate.
     * @return A {@link Condition} that represents concatenation.
     */
    Condition concatenate(ValueType value);

    /**
     * Assigns the operation to "!="
     *
     * @param value The {@link ValueType} that we express inequality to.
     * @return A {@link Condition} that represents inequality between this and the parameter.
     */
    Condition isNot(ValueType value);

    /**
     * Assigns the operation to "!="
     *
     * @param value The {@link ValueType} that we express inequality to.
     * @return A {@link Condition} that represents inequality between this and the parameter.
     * @see #notEq(ValueType)
     */
    Condition notEq(ValueType value);

    /**
     * Assigns operation to "&gt;"
     *
     * @param value The {@link ValueType} that this {@link ITypeConditional} is greater than.
     * @return A {@link Condition} that represents greater than between this and the parameter.
     */
    Condition greaterThan(ValueType value);

    /**
     * Assigns operation to "&gt;="
     *
     * @param value The {@link ValueType} that this {@link ITypeConditional} is greater than or equal to.
     * @return A {@link Condition} that represents greater than or equal between this and the parameter.
     */
    Condition greaterThanOrEq(ValueType value);


    /**
     * Assigns operation to "&lt;"
     *
     * @param value The {@link ValueType} that this {@link ITypeConditional} is less than.
     * @return A {@link Condition} that represents less than between this and the parameter.
     */
    Condition lessThan(ValueType value);


    /**
     * Assigns operation to "&lt;="
     *
     * @param value The {@link ValueType} that this {@link ITypeConditional} is less than or equal to.
     * @return A {@link Condition} that represents less than or equal to between this and the parameter.
     */
    Condition lessThanOrEq(ValueType value);

    Condition.Between between(ValueType value);

    /**
     * Turns this {@link ITypeConditional} into an {@link Condition.In}. It means that this object should
     * be represented by the set of {@link ValueType} provided to follow.
     *
     * @param firstValue The first value (required to enforce >= 1)
     * @param values     The rest of the values to pass optionally.
     * @return A new {@link Condition.In} built from this {@link ITypeConditional}.
     */
    Condition.In in(ValueType firstValue, ValueType... values);

    /**
     * Turns this {@link ITypeConditional} into an {@link Condition.In} (not). It means that this object should NOT
     * be represented by the set of {@link ValueType} provided to follow.
     *
     * @param firstValue The first value (required to enforce >= 1)
     * @param values     The rest of the values to pass optionally.
     * @return A new {@link Condition.In} (not) built from this {@link ITypeConditional}.
     */
    Condition.In notIn(ValueType firstValue, ValueType... values);

    /**
     * Turns this {@link ITypeConditional} into an {@link Condition.In}. It means that this object should
     * be represented by the set of {@link ValueType} provided to follow.
     *
     * @param firstValue The first value (required to enforce >= 1)
     * @param values     The rest of the values to pass optionally.
     * @return A new {@link Condition.In} built from this {@link ITypeConditional}.
     */
    Condition.In in(Collection<ValueType> values);

    /**
     * Turns this {@link ITypeConditional} into an {@link Condition.In} (not). It means that this object should NOT
     * be represented by the set of {@link ValueType} provided to follow.
     *
     * @param firstValue The first value (required to enforce >= 1)
     * @param values     The rest of the values to pass optionally.
     * @return A new {@link Condition.In} (not) built from this {@link ITypeConditional}.
     */
    Condition.In notIn(Collection<ValueType> values);


}
