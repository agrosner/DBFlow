package com.raizlabs.android.dbflow.sql.migration;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.property.IndexProperty;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Allows you to specify if and when an {@link IndexProperty} gets used or created.
 */
public abstract class IndexPropertyMigration extends BaseMigration {

    @NonNull
    public abstract IndexProperty getIndexProperty();

    /**
     * @return true if create the index, false to drop the index.
     */
    public boolean shouldCreate() {
        return true;
    }

    @Override
    public void migrate(@NonNull DatabaseWrapper database) {
        if (shouldCreate()) {
            getIndexProperty().createIfNotExists(database);
        } else {
            getIndexProperty().drop(database);
        }
    }
}
