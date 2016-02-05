package com.raizlabs.android.dbflow.sql.migration;

import com.raizlabs.android.dbflow.sql.language.property.IndexProperty;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Allows you to specify if and when an {@link IndexProperty} gets used or created.
 */
public class IndexPropertyMigration extends BaseMigration {

    private final IndexProperty indexProperty;
    private boolean shouldCreate = true;

    public IndexPropertyMigration(IndexProperty indexProperty) {
        this.indexProperty = indexProperty;
    }

    /**
     * If you wish to drop the index, set this method to false. Call this in the subclasses constructor.
     *
     * @param shouldCreate
     */
    public void setShouldCreate(boolean shouldCreate) {
        this.shouldCreate = shouldCreate;
    }

    @Override
    public void migrate(DatabaseWrapper database) {
        if (shouldCreate) {
            indexProperty.createIfNotExists(database);
        } else {
            indexProperty.drop(database);
        }
    }
}
