package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Description: Class that represents a SQL order-by.
 */
public class OrderBy implements Query {

    private List<ColumnAlias> columnAliasList = new ArrayList<>();

    private boolean ascending = false;

    private OrderBy() {

    }

    /**
     * @param columnAliases The varg of aliases used in this {@link OrderBy} query.
     * @return A new instance with the specified alias'
     */
    public static OrderBy columns(ColumnAlias... columnAliases) {
        OrderBy orderBy = new OrderBy();
        orderBy.columnAliasList.addAll(Arrays.asList(columnAliases));
        return orderBy;
    }

    /**
     * @param columnNames The varg of column names used in this {@link OrderBy} query.
     * @return A new instance with the specific column names.
     */
    public static OrderBy columns(String... columnNames) {
        OrderBy orderBy = new OrderBy();
        for (String column : columnNames) {
            orderBy.columnAliasList.add(ColumnAlias.column(column));
        }
        return orderBy;
    }

    /**
     * @return Orders the results in ascending order.
     */
    public OrderBy ascending() {
        return setAscending(true);
    }

    /**
     * @return Orders the results in descending order.
     */
    public OrderBy descending() {
        return setAscending(false);
    }

    /**
     * @param isAscending if the ORDER BY is ascending.
     * @return This instance if its ascending or descending.
     */
    public OrderBy setAscending(boolean isAscending) {
        ascending = isAscending;
        return this;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder("ORDER BY ");
        for (int i = 0; i < columnAliasList.size(); i++) {
            if (i > 0) {
                queryBuilder.append(", ");
            }
            queryBuilder.append(columnAliasList.get(i).getAliasName());
        }
        queryBuilder.appendSpace().append(ascending ? "ASC" : "DESC");
        return queryBuilder.getQuery();
    }
}
