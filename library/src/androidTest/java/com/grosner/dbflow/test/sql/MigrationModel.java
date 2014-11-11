package com.grosner.dbflow.test.sql;

import com.grosner.dbflow.annotation.Table;
import com.grosner.dbflow.test.structure.TestModel1;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Table(databaseName = MigrationDatabase.NAME)
public class MigrationModel extends TestModel1 {
}
