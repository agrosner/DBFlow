package com.raizlabs.android.dbflow.test.structure.externalforeignkey;

import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

/**
 * Description:
 */
@ModelContainer
@Table(database=TestDatabase2.class)
public class ForeignParentModel extends TestModel1 {
}
