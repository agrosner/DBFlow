package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.annotation.Collate;
import com.raizlabs.android.dbflow.sql.Query;

/**
 * Description: Class that represents a SQL order-by.
 */
public class OrderBy implements Query {

    private NameAlias column;

    private boolean isAscending;

    private Collate collation;

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
        StringBuilder query = new StringBuilder("ORDER BY ")
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
