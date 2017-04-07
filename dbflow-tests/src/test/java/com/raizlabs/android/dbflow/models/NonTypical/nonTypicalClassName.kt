package com.raizlabs.android.dbflow.models.NonTypical

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table

/**
 * Tests package name capitalized, class name is lower cased.
 */
@Table(database = TestDatabase::class)
class nonTypicalClassName(@PrimaryKey var id: Int = 0)