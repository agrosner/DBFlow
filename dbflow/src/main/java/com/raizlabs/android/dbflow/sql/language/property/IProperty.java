package com.raizlabs.android.dbflow.sql.language.property;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.NameAlias;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.container.ModelContainer;

/**
 * Description: Defines the base interface all property classes implement.
 */
public interface IProperty<P extends IProperty> extends Query {

    /**
     * @param aliasName The name of the alias.
     * @return A new {@link P} that expresses the current column name with the specified Alias name.
     */
    P as(String aliasName);

    /**
     * Adds another property and returns as a new property. i.e p1 + p2
     *
     * @param iProperty the property to add.
     * @return A new instance.
     */
    P plus(IProperty iProperty);

    /**
     * Subtracts another property and returns as a new property. i.e p1 - p2
     *
     * @param iProperty the property to add.
     * @return A new instance.
     */
    P minus(IProperty iProperty);

    /**
     * @return Appends DISTINCT to the property name. This is handy in {@link Method} queries.
     * This distinct {@link P} can only be used with one column within a {@link Method}.
     */
    P distinct();

    /**
     * @return A property that represents the {@link Model} from which it belongs to. This is useful
     * in {@link Join} queries to represent this property.
     * <p/>
     * The resulting {@link P} becomes `tableName`.`columnName`.
     */
    P withTable();

    /**
     * @param tableNameAlias The name of the table to append. This may be different because of complex queries
     *                       that use a {@link NameAlias} for the table name.
     * @return A property that represents the {@link Model} from which it belongs to. This is useful
     * in {@link Join} queries to represent this property.
     * <p/>
     * The resulting column name becomes `tableName`.`columnName`.
     */
    P withTable(NameAlias tableNameAlias);

    /**
     * @return The underlying {@link NameAlias} that represents the name of this property.
     */
    NameAlias getNameAlias();

    /**
     * @return The key used in {@link ModelContainer} referencing.
     */
    String getContainerKey();

    /**
     * @return The key used in placing values into cursor.
     */
    String getCursorKey();

    /**
     * @return the table this property belongs to.
     */
    Class<? extends Model> getTable();
}
