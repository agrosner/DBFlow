package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.ManyToMany;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@ManyToMany(referencedTable = SameManyToManyModel.class, generatedTableClassName = "ManyOfTheSame")
@Table(database = TestDatabase.class)
public class SameManyToManyModel extends TestModel1 {
}
