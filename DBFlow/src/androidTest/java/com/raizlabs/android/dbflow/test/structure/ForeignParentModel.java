package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(databaseName = TestDatabase.NAME)
public class ForeignParentModel extends TestModel1 {
}
