package com.raizlabs.android.dbflow.sql.language.property;

import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.NameAlias;

/**
 * Description: Provides some useful methods for creating {@link IProperty} from non-property types.
 */
public class PropertyFactory {

    /**
     * Converts a char into a {@link CharProperty} as its value represented by string.
     *
     * @param c the char to convert.
     * @return A new property.
     */
    public static CharProperty from(char c) {
        return new CharProperty(null, new NameAlias("'" + c + "'", false).tickName(false));
    }

    /**
     * Converts a int into a {@link IntProperty} as its value represented by string.
     *
     * @param i the int to convert.
     * @return A new property.
     */
    public static IntProperty from(int i) {
        return new IntProperty(null, new NameAlias(i + "", false).tickName(false));
    }

    /**
     * Converts a double into a {@link DoubleProperty} as its value represented by string.
     *
     * @param d the double to convert.
     * @return A new property.
     */
    public static DoubleProperty from(double d) {
        return new DoubleProperty(null, new NameAlias(d + "", false).tickName(false));
    }

    /**
     * Converts a long into a {@link LongProperty} as its value represented by string.
     *
     * @param l the long to convert.
     * @return A new property.
     */
    public static LongProperty from(long l) {
        return new LongProperty(null, new NameAlias(l + "", false).tickName(false));
    }

    /**
     * Converts a float into a {@link FloatProperty} as its value represented by string.
     *
     * @param f the float to convert.
     * @return A new property.
     */
    public static FloatProperty from(float f) {
        return new FloatProperty(null, new NameAlias(f + "", false).tickName(false));
    }

    /**
     * Converts a short into a {@link ShortProperty} as its value represented by string.
     *
     * @param s the short to convert.
     * @return A new property.
     */
    public static ShortProperty from(short s) {
        return new ShortProperty(null, new NameAlias(s + "", false).tickName(false));
    }

    /**
     * Converts a byte into a {@link ByteProperty} as its value represented by string.
     *
     * @param b the byte to convert.
     * @return A new property.
     */
    public static ByteProperty from(byte b) {
        return new ByteProperty(null, new NameAlias(b + "", false).tickName(false));
    }

    /**
     * Creates a new type-parameterized {@link Property} to be used as its value represented by a string
     * using {@link Condition#convertValueToString(Object)}.
     *
     * @param type The object with value to use.
     * @param <T>  The parameter of its type.
     * @return A new property with its type.
     */
    public static <T> Property<T> from(@Nullable T type) {
        return new Property<>(null, new NameAlias(Condition.convertValueToString(type)).tickName(false));
    }

    /**
     * Creates a new type-parameterized {@link Property} to be used as its value represented by the string passed in.
     *
     * @param type                 The type to return.
     * @param stringRepresentation The string representation of the object you wish to use.
     * @param <T>                  The parameter of its type.
     * @return A new property with its type.
     */
    public static <T> Property<T> from(@Nullable Class<T> type, String stringRepresentation) {
        return new Property<>(null, new NameAlias(stringRepresentation, false).tickName(false));
    }
}
