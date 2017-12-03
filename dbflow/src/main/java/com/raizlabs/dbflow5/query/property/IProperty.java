package com.raizlabs.dbflow5.query.property;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.dbflow5.sql.Query;
import com.raizlabs.dbflow5.query.Join;
import com.raizlabs.dbflow5.query.Method;
import com.raizlabs.dbflow5.query.NameAlias;
import com.raizlabs.dbflow5.query.OrderBy;
import com.raizlabs.dbflow5.structure.Model;

/**
 * Description: Defines the base interface all property classes implement.
 */
public interface IProperty<P extends IProperty> extends Query {

    /**
     * @param aliasName The name of the alias.
     * @return A new {@link P} that expresses the current column name with the specified Alias name.
     */
    @NonNull
    P as(@NonNull String aliasName);

    /**
     * Adds another property and returns as a new property. i.e p1 + p2
     *
     * @param property the property to add.
     * @return A new instance.
     */
    @NonNull
    P plus(@NonNull IProperty property);

    /**
     * Subtracts another property and returns as a new property. i.e p1 - p2
     *
     * @param property the property to subtract.
     * @return A new instance.
     */
    @NonNull
    P minus(@NonNull IProperty property);

    /**
     * Divides another property and returns as a new property. i.e p1 / p2
     *
     * @param property the property to divide.
     * @return A new instance.
     */
    @NonNull
    P div(@NonNull IProperty property);

    /**
     * Multiplies another property and returns as a new property. i.e p1 * p2
     *
     * @param property the property to multiply.
     * @return A new instance.
     */
    P times(@NonNull IProperty property);

    /**
     * Modulous another property and returns as a new property. i.e p1 % p2
     *
     * @param property the property to calculate remainder of.
     * @return A new instance.
     */
    @NonNull
    P rem(@NonNull IProperty property);

    /**
     * Concats another property and returns as a new propert.y i.e. p1 || p2
     *
     * @param property The property to concatenate.
     * @return A new instance.
     */
    @NonNull
    P concatenate(@NonNull IProperty property);

    /**
     * @return Appends DISTINCT to the property name. This is handy in {@link Method} queries.
     * This distinct {@link P} can only be used with one column within a {@link Method}.
     */
    @NonNull
    P distinct();

    /**
     * @return A property that represents the {@link Model} from which it belongs to. This is useful
     * in {@link Join} queries to represent this property.
     * <p/>
     * The resulting {@link P} becomes `tableName`.`columnName`.
     */
    @NonNull
    P withTable();

    /**
     * @param tableNameAlias The name of the table to append. This may be different because of complex queries
     *                       that use a {@link NameAlias} for the table name.
     * @return A property that represents the {@link Model} from which it belongs to. This is useful
     * in {@link Join} queries to represent this property.
     * <p/>
     * The resulting column name becomes `tableName`.`columnName`.
     */
    @NonNull
    P withTable(@NonNull NameAlias tableNameAlias);

    /**
     * @return The underlying {@link NameAlias} that represents the name of this property.
     */
    @NonNull
    NameAlias getNameAlias();

    /**
     * @return The key used in placing values into cursor.
     */
    @NonNull
    String getCursorKey();

    /**
     * @return the table this property belongs to.
     */
    @Nullable
    Class<?> getTable();

    @NonNull
    OrderBy asc();

    @NonNull
    OrderBy desc();
}
