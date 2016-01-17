package com.raizlabs.android.dbflow.sql.migration;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.Index;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

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

    @CallSuper
    @Override
    public void onPreMigrate() {
        index = getIndex();
    }

    @Override
    public final void migrate(DatabaseWrapper database) {
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
    public IndexMigration<ModelClass> addColumn(IProperty property) {
        getIndex().and(property);
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
        if (index == null) {
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
