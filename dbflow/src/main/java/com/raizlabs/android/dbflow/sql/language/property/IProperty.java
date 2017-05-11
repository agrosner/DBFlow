package com.raizlabs.android.dbflow.sql.language.property;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Defines the base interface all property classes implement.
 */
public interface IProperty<P extends IProperty> extends Query {

    /**
     * @param aliasName The name of the alias.
     * @return A new {@link P} that expresses the current column name with the specified Alias name.
     */
    @NonNull
    P as(String aliasName);

    /**
     * Adds another property and returns as a new property. i.e p1 + p2
     *
     * @param iProperty the property to add.
     * @return A new instance.
     */
    @NonNull
    P plus(IProperty iProperty);

    /**
     * Subtracts another property and returns as a new property. i.e p1 - p2
     *
     * @param iProperty the property to subtract.
     * @return A new instance.
     */
    @NonNull
    P minus(IProperty iProperty);

    /**
     * Divides another property and returns as a new property. i.e p1 / p2
     *
     * @param iProperty the property to divide.
     * @return A new instance.
     */
    @NonNull
    P div(IProperty iProperty);

    /**
     * Multiplies another property and returns as a new property. i.e p1 * p2
     *
     * @param iProperty the property to multiply.
     * @return A new instance.
     */
    P times(IProperty iProperty);

    /**
     * Modulous another property and returns as a new property. i.e p1 % p2
     *
     * @param iProperty the property to calculate remainder of.
     * @return A new instance.
     */
    @NonNull
    P rem(IProperty iProperty);

    /**
     * Concats another property and returns as a new propert.y i.e. p1 || p2
     *
     * @param iProperty The property to concatenate.
     * @return A new instance.
     */
    @NonNull
    P concatenate(IProperty iProperty);

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
    P withTable(NameAlias tableNameAlias);

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
    @NonNull
    Class<?> getTable();

    @NonNull
    OrderBy asc();

    @NonNull
    OrderBy desc();
}
