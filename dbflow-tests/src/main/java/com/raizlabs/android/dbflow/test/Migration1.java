package com.raizlabs.android.dbflow.test;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description:
 */
@Migration(version = 2, database = AppDatabase.class)
public class Migration1 extends BaseMigration {

    @Override
    public void migrate(DatabaseWrapper database) {

    }
}
