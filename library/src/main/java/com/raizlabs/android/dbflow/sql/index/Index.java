package com.raizlabs.android.dbflow.sql.index;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Describes a SQLite Index
 */
public class Index<ModelClass extends Model> {

    private final String mIndex;

    private Class<ModelClass> mTable;

    private String[] mColumns;

    private boolean isUnique = false;

    /**
     * Creates a new index with the specified name
     *
     * @param indexName The name of this index.
     */
    public Index(@NonNull String indexName) {
        mIndex = indexName;
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
        mColumns = columns;
        return this;
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
        QueryBuilder query = new QueryBuilder("CREATE ")
                .append(isUnique ? "UNIQUE" : "")
                .append(" INDEX")
                .appendSpaceSeparated(mIndex)
                .append("ON").appendSpaceSeparated(FlowManager.getTableName(mTable))
                .append("(").appendArray(mColumns).append(")");
        databaseDefinition.getWritableDatabase().execSQL(query.getQuery());
    }

    /**
     * Disables the TRIGGER
     */
    public void disable() {
        SqlUtils.dropIndex(mTable, mIndex);
    }

}
