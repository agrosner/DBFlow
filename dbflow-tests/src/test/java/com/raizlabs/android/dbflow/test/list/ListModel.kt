package com.raizlabs.android.dbflow.test.list

import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.test.structure.TestModel1

/**
 * Description:
 */
@Table(database = ListDatabase::class)
class ListModel : TestModel1()
