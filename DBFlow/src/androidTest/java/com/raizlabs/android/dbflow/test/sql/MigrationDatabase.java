package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Description:
 */
@Database(version = 2, name = MigrationDatabase.NAME)
public class MigrationDatabase {

    public static final String NAME = "Migrations";
}
