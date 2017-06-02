package com.raizlabs.android.dbflow.sql.migration;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.Index;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Defines and enables an Index structurally through a migration.
 */
public abstract class IndexMigration<TModel> extends BaseMigration {

    /**
     * The table to index on
     */
    private Class<TModel> onTable;

    /**
     * The name of this index
     */
    private String name;

    /**
     * The underlying index object.
     */
    private Index<TModel> index;

    public IndexMigration(@NonNull Class<TModel> onTable) {
        this.onTable = onTable;
    }

    @NonNull
    public abstract String getName();

    @CallSuper
    @Override
    public void onPreMigrate() {
        index = getIndex();
    }

    @Override
    public final void migrate(@NonNull DatabaseWrapper database) {
        database.execSQL(getIndex().getQuery());
    }

    @CallSuper
    @Override
    public void onPostMigrate() {
        onTable = null;
        name = null;
        index = null;
    }

    /**
     * Adds a column to the underlying INDEX
     *
     * @param property The name of the column to add to the Index
     * @return This migration
     */
    @NonNull
    public IndexMigration<TModel> addColumn(IProperty property) {
        getIndex().and(property);
        return this;
    }

    /**
     * Sets the INDEX to UNIQUE
     *
     * @return This migration.
     */
    @NonNull
    public IndexMigration<TModel> unique() {
        getIndex().unique(true);
        return this;
    }

    /**
     * @return The index object based on the contents of this migration.
     */
    @NonNull
    public Index<TModel> getIndex() {
        if (index == null) {
            index = new Index<TModel>(name).on(onTable);
        }
        return index;
    }

    /**
     * @return the query backing this migration.
     */
    @NonNull
    public String getIndexQuery() {
        return getIndex().getQuery();
    }

}
