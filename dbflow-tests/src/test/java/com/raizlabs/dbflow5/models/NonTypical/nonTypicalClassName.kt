package com.raizlabs.dbflow5.models.NonTypical

import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.annotation.PrimaryKey
import com.raizlabs.dbflow5.annotation.Table

/**
 * Tests package name capitalized, class name is lower cased.
 */
@Table(database = TestDatabase::class)
class nonTypicalClassName(@PrimaryKey var id: Int = 0)