package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

/**
 * Description:
 */
@Table(database = MigrationDatabase.class)
public class MigrationModel extends TestModel1 {
}
