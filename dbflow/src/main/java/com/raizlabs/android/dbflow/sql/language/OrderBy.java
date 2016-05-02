package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.annotation.Collate;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;

/**
 * Description: Class that represents a SQL order-by.
 */
public class OrderBy implements Query {

    public static final String ASCENDING = "ASC";

    public static final String DESCENDING = "DESC";

    private NameAlias column;

    private boolean isAscending;

    private Collate collation;
    private String orderByString;

    public static OrderBy fromProperty(IProperty property) {
        return new OrderBy(property.getNameAlias());
    }

    public static OrderBy fromNameAlias(NameAlias nameAlias) {
        return new OrderBy(nameAlias);
    }

    public static OrderBy fromString(String orderByString) {
        return new OrderBy(orderByString);
    }

    OrderBy(NameAlias column) {
        this.column = column;
    }

    OrderBy(NameAlias column, boolean isAscending) {
        this(column);
        this.isAscending = isAscending;
    }

    OrderBy(String orderByString) {
        this.orderByString = orderByString;
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
        if (orderByString == null) {
            StringBuilder query = new StringBuilder()
                    .append(column)
                    .append(" ");
            if (collation != null) {
                query.append("COLLATE").append(" ").append(collation).append(" ");
            }
            query.append(isAscending ? "ASC" : "DESC");
            return query.toString();
        } else {
            return orderByString;
        }
    }

    @Override
    public String toString() {
        return getQuery();
    }
}
