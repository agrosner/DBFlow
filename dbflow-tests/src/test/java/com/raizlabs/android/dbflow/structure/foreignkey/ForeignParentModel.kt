package com.raizlabs.android.dbflow.structure.foreignkey

import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.structure.TestModel1

/**
 * Description:
 */
@Table(database = TestDatabase::class)
open class ForeignParentModel : TestModel1()
