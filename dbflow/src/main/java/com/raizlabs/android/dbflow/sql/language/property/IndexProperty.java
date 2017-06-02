package com.raizlabs.android.dbflow.sql.language.property;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.Index;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Defines an INDEX in Sqlite. It basically speeds up data retrieval over large datasets.
 * It gets generated from {@link Table#indexGroups()}, but also can be manually constructed. These are activated
 * and deactivated manually.
 */
public class IndexProperty<T> {

    private final Index<T> index;

    public IndexProperty(String indexName, boolean unique, Class<T> table, IProperty... properties) {
        index = SQLite.index(indexName);
        index.on(table, properties)
            .unique(unique);
    }

    public void createIfNotExists(@NonNull DatabaseWrapper wrapper) {
        index.enable(wrapper);
    }

    public void createIfNotExists() {
        index.enable();
    }

    public void drop() {
        index.disable();
    }

    public void drop(DatabaseWrapper writableDatabase) {
        index.disable(writableDatabase);
    }

    public Index<T> getIndex() {
        return index;
    }

    public String getIndexName() {
        return QueryBuilder.quoteIfNeeded(index.getIndexName());
    }
}
