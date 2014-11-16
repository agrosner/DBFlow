package com.grosner.processor.model.builder;


import com.grosner.dbflow.sql.QueryBuilder;
import com.grosner.dbflow.annotation.Column;
import com.grosner.dbflow.annotation.ForeignKeyReference;
import com.grosner.processor.utils.ModelUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: andrewgrosner
 * Description: Wraps around creating the table creation in a simple way.
 */
public class TableCreationQueryBuilder extends QueryBuilder<TableCreationQueryBuilder> {

    /**
     * Appends a column to this class. It will append the correct string value based on the {@link com.grosner.dbflow.annotation.Column}
     *
     * @param column
     * @return
     */
    public QueryBuilder appendColumn(Column column) {
        if (column.length() > -1) {
            mQuery.append("(");
            mQuery.append(column.length());
            mQuery.append(")");
        }

        if (column.columnType() == Column.PRIMARY_KEY_AUTO_INCREMENT) {
            mQuery.append(" PRIMARY KEY AUTOINCREMENT");
        }

        if (column.notNull()) {
            mQuery.append(" NOT NULL ON CONFLICT ");
            mQuery.append(column.onNullConflict().toString());
        }

        if (column.unique()) {
            mQuery.append(" UNIQUE ON CONFLICT ");
            mQuery.append(column.onUniqueConflict().toString());
        }
        return this;
    }

    /**
     * Appends this to the beginning of the query.
     *
     * @param tableName The name of the table to pass in
     * @return This instance
     */
    public QueryBuilder appendCreateTableIfNotExists(String tableName) {
        mQuery.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append("(");
        return this;
    }

    public void appendForeignKeys(ForeignKeyReference[] references) {
        QueryBuilder queryBuilder;
        List<QueryBuilder> queryBuilders = new ArrayList<QueryBuilder>();
        for (ForeignKeyReference foreignKeyReference : references) {
            queryBuilder = new QueryBuilder().append(foreignKeyReference.columnName())
                    .appendSpace()
                    .appendType(ModelUtils.getClassFromAnnotation(foreignKeyReference));
            queryBuilders.add(queryBuilder);
        }

        appendList(queryBuilders);
    }
}
