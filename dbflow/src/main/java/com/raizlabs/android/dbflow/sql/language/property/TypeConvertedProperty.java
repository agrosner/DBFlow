package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.sql.language.NameAlias;

/**
 * Description: Provides convenience methods for {@link TypeConverter} when constructing queries.
 *
 * @author Andrew Grosner (fuzz)
 */

public class TypeConvertedProperty<T, V> extends Property<T> {

    private Property<V> databaseProperty;

    public TypeConvertedProperty(Class<?> table, NameAlias nameAlias) {
        super(table, nameAlias);
    }

    public TypeConvertedProperty(Class<?> table, String columnName) {
        super(table, columnName);
    }

    /**
     * @return A new {@link Property} that corresponds to the inverted type of the {@link TypeConvertedProperty}.
     * Provides a convenience for supplying type converted methods within the DataClass of the {@link TypeConverter}
     */
    public Property<V> databaseProperty() {
        if (databaseProperty == null) {
            databaseProperty = new Property<>(table, nameAlias);
        }
        return databaseProperty;
    }
}
