package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
    @NonNull
    Operator<T> is(@Nullable T value);

    /**
     * Assigns the operation to "=". Identical to {@link #is(T)}
     *
     * @param value The {@link T} that we express equality to.
     * @return A {@link Operator} that represents equality between this and the parameter.
     * @see #is(T)
     */
    @NonNull
    Operator<T> eq(@Nullable T value);

    /**
     * Generates a {@link Operator} that concatenates this {@link IOperator} with the {@link T} via "||"
     * by columnName=columnName || value
     *
     * @param value The value to concatenate.
     * @return A {@link Operator<T>} that represents concatenation.
     */
    @NonNull
    Operator<T> concatenate(@Nullable T value);

    /**
     * Assigns the operation to "!="
     *
     * @param value The {@link T} that we express inequality to.
     * @return A {@link Operator<T>} that represents inequality between this and the parameter.
     */
    @NonNull
    Operator<T> isNot(@Nullable T value);

    /**
     * Assigns the operation to "!="
     *
     * @param value The {@link T} that we express inequality to.
     * @return A {@link Operator<T>} that represents inequality between this and the parameter.
     * @see #notEq(T)
     */
    @NonNull
    Operator<T> notEq(@Nullable T value);

    /**
     * Assigns operation to "&gt;"
     *
     * @param value The {@link T} that this {@link IOperator} is greater than.
     * @return A {@link Operator<T>} that represents greater than between this and the parameter.
     */
    @NonNull
    Operator<T> greaterThan(@NonNull T value);

    /**
     * Assigns operation to "&gt;="
     *
     * @param value The {@link T} that this {@link IOperator} is greater than or equal to.
     * @return A {@link Operator<T>} that represents greater than or equal between this and the parameter.
     */
    @NonNull
    Operator<T> greaterThanOrEq(@NonNull T value);


    /**
     * Assigns operation to "&lt;"
     *
     * @param value The {@link T} that this {@link IOperator} is less than.
     * @return A {@link Operator<T>} that represents less than between this and the parameter.
     */
    @NonNull
    Operator<T> lessThan(@NonNull T value);


    /**
     * Assigns operation to "&lt;="
     *
     * @param value The {@link T} that this {@link IOperator} is less than or equal to.
     * @return A {@link Operator<T>} that represents less than or equal to between this and the parameter.
     */
    @NonNull
    Operator<T> lessThanOrEq(@NonNull T value);

    @NonNull
    Operator.Between<T> between(@NonNull T value);

    /**
     * Turns this {@link IOperator} into an {@link Operator<T>.In}. It means that this object should
     * be represented by the set of {@link T} provided to follow.
     *
     * @param firstValue The first value (required to enforce >= 1)
     * @param values     The rest of the values to pass optionally.
     * @return A new {@link Operator<T>.In} built from this {@link IOperator}.
     */
    @NonNull
    @SuppressWarnings("unchecked")
    Operator.In<T> in(@NonNull T firstValue, T... values);

    /**
     * Turns this {@link IOperator} into an {@link Operator<T>.In} (not). It means that this object should NOT
     * be represented by the set of {@link T} provided to follow.
     *
     * @param firstValue The first value (required to enforce >= 1)
     * @param values     The rest of the values to pass optionally.
     * @return A new {@link Operator<T>.In} (not) built from this {@link IOperator}.
     */
    @NonNull
    @SuppressWarnings("unchecked")
    Operator.In<T> notIn(@NonNull T firstValue, T... values);

    /**
     * Turns this {@link IOperator} into an {@link Operator<T>.In}. It means that this object should
     * be represented by the set of {@link T} provided to follow.
     *
     * @param values The rest of the values to pass optionally.
     * @return A new {@link Operator<T>.In} built from this {@link IOperator}.
     */
    @NonNull
    Operator.In<T> in(@NonNull Collection<T> values);

    /**
     * Turns this {@link IOperator} into an {@link Operator<T>.In} (not). It means that this object should NOT
     * be represented by the set of {@link T} provided to follow.
     *
     * @param values The rest of the values to pass optionally.
     * @return A new {@link Operator<T>.In} (not) built from this {@link IOperator}.
     */
    @NonNull
    Operator.In<T> notIn(@NonNull Collection<T> values);

    /**
     * Adds another value and returns the operator. i.e p1 + p2
     *
     * @param value the value to add.
     */
    @NonNull
    Operator<T> plus(@NonNull T value);

    /**
     * Subtracts another value and returns the operator. i.e p1 - p2
     *
     * @param value the value to subtract.
     */
    @NonNull
    Operator<T> minus(@NonNull T value);

    /**
     * Divides another value and returns as the operator. i.e p1 / p2
     *
     * @param value the value to divide.
     * @return A new instance.
     */
    @NonNull
    Operator<T> div(@NonNull T value);

    /**
     * Multiplies another value and returns as the operator. i.e p1 * p2
     *
     * @param value the value to multiply.
     */
    Operator<T> times(@NonNull T value);

    /**
     * Modulous another value and returns as the operator. i.e p1 % p2
     *
     * @param value the value to calculate remainder of.
     */
    @NonNull
    Operator<T> rem(@NonNull T value);
}
