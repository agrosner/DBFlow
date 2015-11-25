package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.annotation.Collate;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;

/**
 * Description: Class that represents a SQL order-by.
 */
public class OrderBy implements Query {

    private NameAlias column;

    private boolean isAscending;

    private Collate collation;

    public static OrderBy fromProperty(IProperty property) {
        return new OrderBy(property.getNameAlias());
    }

    public static OrderBy fromNameAlias(NameAlias nameAlias) {
        return new OrderBy(nameAlias);
    }

    OrderBy(NameAlias column) {
        this.column = column;
    }

    OrderBy(NameAlias column, boolean isAscending) {
        this(column);
        this.isAscending = isAscending;
    }

    public OrderBy ascending() {
        isAscending = true;
        return this;
    }

    public OrderBy descending() {
        isAscending = false;
        return this;
    }

    public OrderBy collate(Collate collate) {
        this.collation = collate;
        return this;
    }

    @Override
    public String getQuery() {
        StringBuilder query = new StringBuilder()
                .append(column)
                .append(" ");
        if (collation != null) {
            query.append("COLLATE").append(" ").append(collation).append(" ");
        }
        query.append(isAscending ? "ASC" : "DESC");
        return query.toString();
    }

    @Override
    public String toString() {
        return getQuery();
    }
}
