package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Table(databaseName = MigrationDatabase.NAME)
public class MigrationModel extends TestModel1 {
}
