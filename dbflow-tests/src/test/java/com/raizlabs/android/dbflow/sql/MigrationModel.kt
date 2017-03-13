package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.TestModel1

/**
 * Description:
 */
@Table(database = MigrationDatabase::class)
class MigrationModel : TestModel1()
