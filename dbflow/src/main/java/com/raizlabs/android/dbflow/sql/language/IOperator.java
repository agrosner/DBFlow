package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;

import java.util.Collection;

/**
 * Description: Interface for objects that can be used as {@link Operator} that have a type parameter.
 */
public interface IOperator<T> extends Query, IConditional {

    /**
     * Assigns the operation to "="
     *
     * @param value The {@link T} that we express equality to.
     * @return A {@link Operator} that represents equality between this and the parameter.
     */
    Operator<T> is(T value);

    /**
     * Assigns the operation to "=". Identical to {@link #is(T)}
     *
     * @param value The {@link T} that we express equality to.
     * @return A {@link Operator} that represents equality between this and the parameter.
     * @see #is(T)
     */
    Operator<T> eq(T value);

    /**
     * Generates a {@link Operator} that concatenates this {@link IOperator} with the {@link T} via "||"
     * by columnName=columnName || value
     *
     * @param value The value to concatenate.
     * @return A {@link Operator<T>} that represents concatenation.
     */
    Operator<T> concatenate(T value);

    /**
     * Assigns the operation to "!="
     *
     * @param value The {@link T} that we express inequality to.
     * @return A {@link Operator<T>} that represents inequality between this and the parameter.
     */
    Operator<T> isNot(T value);

    /**
     * Assigns the operation to "!="
     *
     * @param value The {@link T} that we express inequality to.
     * @return A {@link Operator<T>} that represents inequality between this and the parameter.
     * @see #notEq(T)
     */
    Operator<T> notEq(T value);

    /**
     * Assigns operation to "&gt;"
     *
     * @param value The {@link T} that this {@link IOperator} is greater than.
     * @return A {@link Operator<T>} that represents greater than between this and the parameter.
     */
    Operator<T> greaterThan(T value);

    /**
     * Assigns operation to "&gt;="
     *
     * @param value The {@link T} that this {@link IOperator} is greater than or equal to.
     * @return A {@link Operator<T>} that represents greater than or equal between this and the parameter.
     */
    Operator<T> greaterThanOrEq(T value);


    /**
     * Assigns operation to "&lt;"
     *
     * @param value The {@link T} that this {@link IOperator} is less than.
     * @return A {@link Operator<T>} that represents less than between this and the parameter.
     */
    Operator<T> lessThan(T value);


    /**
     * Assigns operation to "&lt;="
     *
     * @param value The {@link T} that this {@link IOperator} is less than or equal to.
     * @return A {@link Operator<T>} that represents less than or equal to between this and the parameter.
     */
    Operator<T> lessThanOrEq(T value);

    Operator.Between<T> between(T value);

    /**
     * Turns this {@link IOperator} into an {@link Operator<T>.In}. It means that this object should
     * be represented by the set of {@link T} provided to follow.
     *
     * @param firstValue The first value (required to enforce >= 1)
     * @param values     The rest of the values to pass optionally.
     * @return A new {@link Operator<T>.In} built from this {@link IOperator}.
     */
    @SuppressWarnings("unchecked")
    Operator.In<T> in(T firstValue, T... values);

    /**
     * Turns this {@link IOperator} into an {@link Operator<T>.In} (not). It means that this object should NOT
     * be represented by the set of {@link T} provided to follow.
     *
     * @param firstValue The first value (required to enforce >= 1)
     * @param values     The rest of the values to pass optionally.
     * @return A new {@link Operator<T>.In} (not) built from this {@link IOperator}.
     */
    @SuppressWarnings("unchecked")
    Operator.In<T> notIn(T firstValue, T... values);

    /**
     * Turns this {@link IOperator} into an {@link Operator<T>.In}. It means that this object should
     * be represented by the set of {@link T} provided to follow.
     *
     * @param values The rest of the values to pass optionally.
     * @return A new {@link Operator<T>.In} built from this {@link IOperator}.
     */
    Operator.In<T> in(Collection<T> values);

    /**
     * Turns this {@link IOperator} into an {@link Operator<T>.In} (not). It means that this object should NOT
     * be represented by the set of {@link T} provided to follow.
     *
     * @param values The rest of the values to pass optionally.
     * @return A new {@link Operator<T>.In} (not) built from this {@link IOperator}.
     */
    Operator.In<T> notIn(Collection<T> values);


}
