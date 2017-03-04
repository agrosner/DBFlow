package com.grosner.dbflow.jack.tests;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description:
 */
@Migration(version = 3, database = AppDatabase.class)
public class Migration2 extends BaseMigration {
    @Override
    public void migrate(DatabaseWrapper database) {

    }
}
