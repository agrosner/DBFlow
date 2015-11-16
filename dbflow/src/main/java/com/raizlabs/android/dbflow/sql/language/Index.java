package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: an INDEX class that enables you to index a specific column from a table. This enables
 * faster retrieval on tables, while increasing the database file size. So enable/disable these as necessary.
 */
public class Index<ModelClass extends Model> implements Query {

    private final String indexName;
    private Class<ModelClass> table;
    private List<NameAlias> columns;
    private boolean isUnique = false;

    /**
     * Creates a new index with the specified name
     *
     * @param indexName The name of this index.
     */
    public Index(@NonNull String indexName) {
        this.indexName = indexName;
        columns = new ArrayList<>();
    }

    /**
     * If true, will append the UNIQUE statement to this trigger.
     *
     * @param unique true if unique. If created again, a {@link android.database.SQLException} is thrown.
     * @return This instance.
     */
    public Index<ModelClass> unique(boolean unique) {
        isUnique = unique;
        return this;
    }

    /**
     * The table to execute this Index on.
     *
     * @param table      The table to execute index on.
     * @param properties The properties to create an index for.
     * @return This instance.
     */
    public Index<ModelClass> on(@NonNull Class<ModelClass> table, IProperty... properties) {
        this.table = table;
        for (IProperty property : properties) {
            and(property);
        }
        return this;
    }

    /**
     * The table to execute this Index on.
     *
     * @param table   The table to execute index on.
     * @param columns The columns to create an index for.
     * @return This instance.
     */
    public Index<ModelClass> on(@NonNull Class<ModelClass> table, NameAlias firstAlias, NameAlias... columns) {
        this.table = table;
        and(firstAlias);
        for (NameAlias column : columns) {
            and(column);
        }
        return this;
    }

    /**
     * Appends a column to this index list.
     *
     * @param property The name of the column. If already exists, this column will not be added
     * @return This instance.
     */
    public Index<ModelClass> and(IProperty property) {
        if (!columns.contains(property.getNameAlias())) {
            columns.add(property.getNameAlias());
        }
        return this;
    }

    /**
     * Appends a column to this index list.
     *
     * @param columnName The name of the column. If already exists, this column will not be added
     * @return This instance.
     */
    public Index<ModelClass> and(NameAlias columnName) {
        if (!columns.contains(columnName)) {
            columns.add(columnName);
        }
        return this;
    }

    /**
     * @return The name of this index.
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * @return The table this INDEX belongs to.
     */
    public Class<ModelClass> getTable() {
        return table;
    }

    /**
     * @return true if the index is unique
     */
    public boolean isUnique() {
        return isUnique;
    }

    /**
     * Enables the TRIGGER.
     */
    public void enable() {

        if (table == null) {
            throw new IllegalStateException("Please call on() to set a table to use this index on.");
        } else if (columns == null || columns.isEmpty()) {
            throw new IllegalStateException("There should be at least one column in this index");
        }

        BaseDatabaseDefinition databaseDefinition = FlowManager.getDatabaseForTable(table);
        databaseDefinition.getWritableDatabase().execSQL(getQuery());
    }

    /**
     * Disables the TRIGGER
     */
    public void disable() {
        SqlUtils.dropIndex(table, indexName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getQuery() {
        return new QueryBuilder("CREATE ")
                .append(isUnique ? "UNIQUE " : "")
                .append("INDEX IF NOT EXISTS ")
                .appendQuotedIfNeeded(indexName)
                .append(" ON ").append(FlowManager.getTableName(table))
                .append("(").appendList(columns).append(")").getQuery();
    }
}
