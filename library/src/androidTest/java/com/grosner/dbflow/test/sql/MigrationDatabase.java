package com.grosner.dbflow.test.sql;

import com.grosner.dbflow.annotation.Database;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Database(version = 1, name = MigrationDatabase.NAME)
public class MigrationDatabase {

    public static final String NAME = "Migrations";
}
