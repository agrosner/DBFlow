package com.raizlabs.android.dbflow.sql.language.property;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;

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
        return new CharProperty(null, NameAlias.rawBuilder("'" + c + "'")
                .build());
    }

    /**
     * Converts a int into a {@link IntProperty} as its value represented by string.
     *
     * @param i the int to convert.
     * @return A new property.
     */
    public static IntProperty from(int i) {
        return new IntProperty(null, NameAlias.rawBuilder(i + "")
                .build());
    }

    /**
     * Converts a double into a {@link DoubleProperty} as its value represented by string.
     *
     * @param d the double to convert.
     * @return A new property.
     */
    public static DoubleProperty from(double d) {
        return new DoubleProperty(null, NameAlias.rawBuilder(d + "")
                .build());
    }

    /**
     * Converts a long into a {@link LongProperty} as its value represented by string.
     *
     * @param l the long to convert.
     * @return A new property.
     */
    public static LongProperty from(long l) {
        return new LongProperty(null, NameAlias.rawBuilder(l + "")
                .build());
    }

    /**
     * Converts a float into a {@link FloatProperty} as its value represented by string.
     *
     * @param f the float to convert.
     * @return A new property.
     */
    public static FloatProperty from(float f) {
        return new FloatProperty(null, NameAlias.rawBuilder(f + "")
                .build());
    }

    /**
     * Converts a short into a {@link ShortProperty} as its value represented by string.
     *
     * @param s the short to convert.
     * @return A new property.
     */
    public static ShortProperty from(short s) {
        return new ShortProperty(null, NameAlias.rawBuilder(s + "")
                .build());
    }

    /**
     * Converts a byte into a {@link ByteProperty} as its value represented by string.
     *
     * @param b the byte to convert.
     * @return A new property.
     */
    public static ByteProperty from(byte b) {
        return new ByteProperty(null, NameAlias.rawBuilder(b + "")
                .build());
    }

    /**
     * Creates a new type-parameterized {@link Property} to be used as its value represented by a string
     * using {@link Condition#convertValueToString(Object)}.
     * <p/>
     * It will not convert a String column name
     * into a property, rather it assumes its database value represented by the String.
     *
     * @param type The object with value to use.
     * @param <T>  The parameter of its type.
     * @return A new property with its type.
     */
    public static <T> Property<T> from(@Nullable T type) {
        return new Property<>(null, NameAlias.rawBuilder(
                Condition.convertValueToString(type))
                .build());
    }

    /**
     * Creates a new {@link Property} that is used to allow selects in a query.
     *
     * @param queriable The queriable to use and evaulated into a query.
     * @param <TModel>  The model class of the query.
     * @return A new property that is a query.
     */
    public static <TModel extends Model> Property<TModel> from(@NonNull ModelQueriable<TModel> queriable) {
        return from(queriable.getTable(), "(" + queriable.getQuery() + ")");
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
        return new Property<>(null, NameAlias.rawBuilder(stringRepresentation)
                .build());
    }
}
