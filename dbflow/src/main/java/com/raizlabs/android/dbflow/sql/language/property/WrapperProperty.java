package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.language.NameAlias;

/**
 * Description: Provides convenience for types that are represented in different ways in the DB.
 *
 * @author Andrew Grosner (fuzz)
 */
public class WrapperProperty<T, V> extends Property<V> {

    private WrapperProperty<V, T> databaseProperty;

    public WrapperProperty(Class<?> table, NameAlias nameAlias) {
        super(table, nameAlias);
    }

    public WrapperProperty(Class<?> table, String columnName) {
        super(table, columnName);
    }

    /**
     * @return A new {@link Property} that corresponds to the inverted type of the {@link WrapperProperty}. Convenience
     * for types that have different DB representations.
     */
    public Property<T> invertProperty() {
        if (databaseProperty == null) {
            databaseProperty = new WrapperProperty<>(table, nameAlias);
        }
        return databaseProperty;
    }
}
