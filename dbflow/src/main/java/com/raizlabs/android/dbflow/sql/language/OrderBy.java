package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

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

    @NonNull
    public static OrderBy fromProperty(@NonNull IProperty property) {
        return new OrderBy(property.getNameAlias());
    }

    @NonNull
    public static OrderBy fromNameAlias(@NonNull NameAlias nameAlias) {
        return new OrderBy(nameAlias);
    }

    @NonNull
    public static OrderBy fromString(@NonNull String orderByString) {
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

    @NonNull
    public OrderBy ascending() {
        isAscending = true;
        return this;
    }

    @NonNull
    public OrderBy descending() {
        isAscending = false;
        return this;
    }

    @NonNull
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
            query.append(isAscending ? ASCENDING : DESCENDING);
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
