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
    private Class<ModelClass> mOnTable;

    /**
     * The name of this index
     */
    private String mName;

    /**
     * The underlying index object.
     */
    private Index<ModelClass> mIndex;

    public IndexMigration(@NonNull String name, @NonNull Class<ModelClass> mOnTable) {
        this.mOnTable = mOnTable;
        this.mName = name;
    }

    @Override
    public void onPreMigrate() {
        super.onPreMigrate();

        mIndex = new Index<ModelClass>(mName).on(mOnTable);
    }

    @Override
    public void migrate(SQLiteDatabase database) {
        getIndex().enable();
    }

    @Override
    public void onPostMigrate() {
        mOnTable = null;
        mName = null;
        mIndex = null;
    }

    /**
     * Adds a column to the underlying INDEX
     *
     * @param columnName The name of the column to add to the Index
     * @return This migration
     */
    public IndexMigration<ModelClass> addColumn(String columnName) {
        mIndex.and(columnName);
        return this;
    }

    /**
     * Sets the INDEX to UNIQUE
     *
     * @return This migration.
     */
    public IndexMigration<ModelClass> unique() {
        mIndex.unique(true);
        return this;
    }

    /**
     * @return The index object based on the contents of this migration.
     */
    public Index<ModelClass> getIndex() {
        return mIndex;
    }

    /**
     * @return the query backing this migration.
     */
    public String getIndexQuery() {
        return getIndex().getQuery();
    }

}
