package com.raizlabs.android.dbflow.sql.language;

import android.text.TextUtils;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;

/**
 * Description: Represents a column name as an alias to its original name. EX: SELECT `money` AS `myMoney`. However
 * if a an asName is not specified, then at its base this simply represents a column.
 */
public class ColumnAlias implements Query {

    /**
     * @param columnName The name of the column that will use an alias in a query.
     * @return A new instance.
     */
    public static ColumnAlias column(String columnName) {
        return new ColumnAlias(columnName);
    }

    private final String columnName;

    private String asName;

    private ColumnAlias(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Appends the asName after the AS of the query.
     *
     * @param asName The name of the AS `someName`
     * @return This instance.
     */
    public ColumnAlias as(String asName) {
        this.asName = asName;
        return this;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.appendQuoted(columnName);
        if (!TextUtils.isEmpty(asName)) {
            queryBuilder.appendSpaceSeparated("AS")
                    .appendQuoted(asName);
        }
        return queryBuilder.getQuery();
    }

    @Override
    public String toString() {
        return getQuery();
    }
}
