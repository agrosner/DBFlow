package com.raizlabs.android.dbflow.sql.language.property;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.Operator;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;

/**
 * Description: Provides some useful methods for creating {@link IProperty} from non-property types.
 */
public class PropertyFactory {

    /**
     * Converts a char into a {@link Property} as its value represented by string.
     *
     * @param c the char to convert.
     * @return A new property.
     */
    @NonNull
    public static Property<Character> from(char c) {
        return new Property<>(null, NameAlias.rawBuilder("'" + c + "'")
            .build());
    }

    /**
     * Converts a int into a {@link Property} as its value represented by string.
     *
     * @param i the int to convert.
     * @return A new property.
     */
    @NonNull
    public static Property<Integer> from(int i) {
        return new Property<>(null, NameAlias.rawBuilder(i + "")
            .build());
    }

    /**
     * Converts a double into a {@link Property} as its value represented by string.
     *
     * @param d the double to convert.
     * @return A new property.
     */
    @NonNull
    public static Property<Double> from(double d) {
        return new Property<>(null, NameAlias.rawBuilder(d + "")
            .build());
    }

    /**
     * Converts a long into a {@link Property} as its value represented by string.
     *
     * @param l the long to convert.
     * @return A new property.
     */
    @NonNull
    public static Property<Long> from(long l) {
        return new Property<>(null, NameAlias.rawBuilder(l + "")
            .build());
    }

    /**
     * Converts a float into a {@link Property} as its value represented by string.
     *
     * @param f the float to convert.
     * @return A new property.
     */
    @NonNull
    public static Property<Float> from(float f) {
        return new Property<>(null, NameAlias.rawBuilder(f + "")
            .build());
    }

    /**
     * Converts a short into a {@link Property} as its value represented by string.
     *
     * @param s the short to convert.
     * @return A new property.
     */
    @NonNull
    public static Property<Short> from(short s) {
        return new Property<>(null, NameAlias.rawBuilder(s + "")
            .build());
    }

    /**
     * Converts a byte into a {@link Property} as its value represented by string.
     *
     * @param b the byte to convert.
     * @return A new property.
     */
    @NonNull
    public static Property<Byte> from(byte b) {
        return new Property<>(null, NameAlias.rawBuilder(b + "")
            .build());
    }

    /**
     * Creates a new type-parameterized {@link Property} to be used as its value represented by a string
     * using {@link Operator#convertValueToString(Object)}.
     * <p/>
     * It will not convert a String column name
     * into a property, rather it assumes its database value represented by the String.
     *
     * @param type The object with value to use.
     * @param <T>  The parameter of its type.
     * @return A new property with its type.
     */
    @NonNull
    public static <T> Property<T> from(@Nullable T type) {
        return new Property<>(null, NameAlias.rawBuilder(
            Operator.convertValueToString(type))
            .build());
    }

    /**
     * Creates a new {@link Property} that is used to allow selects in a query.
     *
     * @param queriable The queriable to use and evaulated into a query.
     * @param <TModel>  The model class of the query.
     * @return A new property that is a query.
     */
    @NonNull
    public static <TModel> Property<TModel> from(@NonNull ModelQueriable<TModel> queriable) {
        return from(queriable.getTable(), "(" + String.valueOf(queriable.getQuery()).trim() + ")");
    }

    /**
     * Creates a new type-parameterized {@link Property} to be used as its value represented by the string passed in.
     *
     * @param type                 The type to return.
     * @param stringRepresentation The string representation of the object you wish to use.
     * @param <T>                  The parameter of its type.
     * @return A new property with its type.
     */
    @NonNull
    public static <T> Property<T> from(@Nullable Class<T> type, String stringRepresentation) {
        return new Property<>(null, NameAlias.rawBuilder(stringRepresentation)
            .build());
    }
}
