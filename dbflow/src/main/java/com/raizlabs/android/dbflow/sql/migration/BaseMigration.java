package com.raizlabs.android.dbflow.sql.migration;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Provides the base implementation of {@link com.raizlabs.android.dbflow.sql.migration.Migration} with
 * only {@link Migration#migrate(DatabaseWrapper)} needing to be implemented.
 */
public abstract class BaseMigration implements Migration {


    @Override
    public void onPreMigrate() {

    }

    @Override
    public abstract void migrate(@NonNull DatabaseWrapper database);

    @Override
    public void onPostMigrate() {

    }
}
