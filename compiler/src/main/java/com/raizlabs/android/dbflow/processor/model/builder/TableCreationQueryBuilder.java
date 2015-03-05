package com.raizlabs.android.dbflow.processor.model.builder;


import com.raizlabs.android.dbflow.processor.definition.ColumnDefinition;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.processor.utils.ModelUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: andrewgrosner
 * Description: Wraps around creating the table creation in a simple way.
 */
public class TableCreationQueryBuilder extends QueryBuilder<TableCreationQueryBuilder> {

    /**
     * Appends a column to this class. It will append the correct string value based on the {@link com.raizlabs.android.dbflow.processor.definition.ColumnDefinition}
     *
     * @param column
     * @return
     */
    public QueryBuilder appendColumn(ColumnDefinition column) {
        if (column.length> -1) {
            mQuery.append("(");
            mQuery.append(column.length);
            mQuery.append(")");
        }

        if (column.columnType == Column.PRIMARY_KEY_AUTO_INCREMENT) {
            append(" PRIMARY KEY AUTOINCREMENT");
        }

        if (column.notNull) {
            appendSpaceSeparated("NOT NULL ON CONFLICT")
                    .append(column.onNullConflict.toString());
        }

        if (column.unique) {
            appendSpaceSeparated("UNIQUE ON CONFLICT")
                    .append(column.onUniqueConflict.toString());
        }

        if(column.collate != null && !column.collate.isEmpty()) {
            appendSpaceSeparated("COLLATE")
                    .append(column.collate);
        }

        if(column.defaultValue != null && !column.defaultValue.isEmpty()) {
            appendSpaceSeparated("DEFAULT")
                    .append(column.defaultValue);
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
        append("CREATE TABLE IF NOT EXISTS ").appendQuoted(tableName).append("(");
        return this;
    }

    public void appendForeignKeys(ForeignKeyReference[] references) {
        QueryBuilder queryBuilder;
        List<QueryBuilder> queryBuilders = new ArrayList<QueryBuilder>();
        for (ForeignKeyReference foreignKeyReference : references) {
            queryBuilder = new QueryBuilder().appendQuoted(foreignKeyReference.columnName())
                    .appendSpace()
                    .appendType(ModelUtils.getClassFromAnnotation(foreignKeyReference));
            queryBuilders.add(queryBuilder);
        }

        appendList(queryBuilders);
    }
}
