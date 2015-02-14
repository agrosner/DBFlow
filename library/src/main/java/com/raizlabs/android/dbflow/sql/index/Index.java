package com.raizlabs.android.dbflow.sql.index;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Describes a SQLite Index
 */
public class Index<ModelClass extends Model> implements Query {

    private final String mIndex;

    private Class<ModelClass> mTable;

    private List<String> mColumns;

    private boolean isUnique = false;

    /**
     * Creates a new index with the specified name
     *
     * @param indexName The name of this index.
     */
    public Index(@NonNull String indexName) {
        mIndex = indexName;
        mColumns = new ArrayList<>();
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
     * @param table   The table to execute index on.
     * @param columns The columns to create an index for.
     * @return This instance.
     */
    public Index<ModelClass> on(@NonNull Class<ModelClass> table, String... columns) {
        mTable = table;
        for (String column : columns) {
            and(column);
        }
        return this;
    }

    /**
     * Appends a column to this index list.
     *
     * @param columnName The name of the column. If already exists, this column will not be added
     * @return This instance.
     */
    public Index<ModelClass> and(String columnName) {
        if (!mColumns.contains(columnName)) {
            mColumns.add(columnName);
        }
        return this;
    }

    /**
     * @return The name of this index.
     */
    public String getIndexName() {
        return mIndex;
    }

    /**
     * @return The table this INDEX belongs to.
     */
    public Class<ModelClass> getTable() {
        return mTable;
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

        if (mTable == null) {
            throw new IllegalStateException("Please call on() to set a table to use this index on.");
        } else if (mColumns == null || mColumns.length == 0) {
            throw new IllegalStateException("There should be at least one column in this index");
        }

        BaseDatabaseDefinition databaseDefinition = FlowManager.getDatabaseForTable(mTable);
        databaseDefinition.getWritableDatabase().execSQL(getQuery());
    }

    /**
     * Disables the TRIGGER
     */
    public void disable() {
        SqlUtils.dropIndex(mTable, mIndex);
    }

    @Override
    public String getQuery() {
        return new QueryBuilder("CREATE ")
                .append(isUnique ? "UNIQUE " : "")
                .append("INDEX IF NOT EXISTS ")
                .appendQuoted(mIndex)
                .append(" ON ").appendQuoted(FlowManager.getTableName(mTable))
                .append("(").appendQuotedArray(mColumns).append(")").getQuery();
    }
}
