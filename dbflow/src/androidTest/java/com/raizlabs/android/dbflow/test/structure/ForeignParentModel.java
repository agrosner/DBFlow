package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(database = TestDatabase.class)
public class ForeignParentModel extends TestModel1 {
}
