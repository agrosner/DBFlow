package com.raizlabs.android.dbflow.structure

import com.raizlabs.android.dbflow.annotation.ManyToMany
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:
 */
@ManyToMany(referencedTable = SameManyToManyModel::class, generatedTableClassName = "ManyOfTheSame")
@Table(database = TestDatabase::class)
class SameManyToManyModel : TestModel1()
