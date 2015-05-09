package com.raizlabs.android.dbflow.sql.migration;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.index.Index;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Defines and enables an Index structurally through a migration.
 */
public class IndexMigration<ModelClass extends Model> extends BaseMigration {

    /**
     * The table to index on
     */
    private Class<ModelClass> onTable;

    /**
     * The name of this index
     */
    private String name;

    /**
     * The underlying index object.
     */
    private Index<ModelClass> index;

    public IndexMigration(@NonNull String name, @NonNull Class<ModelClass> onTable) {
        this.onTable = onTable;
        this.name = name;
    }

    @Override
    public void onPreMigrate() {
        index = getIndex();
    }

    @Override
    public final void migrate(SQLiteDatabase database) {
        database.execSQL(getIndex().getQuery());
    }

    @Override
    public void onPostMigrate() {
        onTable = null;
        name = null;
        index = null;
    }

    /**
     * Adds a column to the underlying INDEX
     *
     * @param columnName The name of the column to add to the Index
     * @return This migration
     */
    public IndexMigration<ModelClass> addColumn(String columnName) {
        getIndex().and(columnName);
        return this;
    }

    /**
     * Sets the INDEX to UNIQUE
     *
     * @return This migration.
     */
    public IndexMigration<ModelClass> unique() {
        getIndex().unique(true);
        return this;
    }

    /**
     * @return The index object based on the contents of this migration.
     */
    public Index<ModelClass> getIndex() {
        if(index == null) {
            index = new Index<ModelClass>(name).on(onTable);
        }
        return index;
    }

    /**
     * @return the query backing this migration.
     */
    public String getIndexQuery() {
        return getIndex().getQuery();
    }

}
