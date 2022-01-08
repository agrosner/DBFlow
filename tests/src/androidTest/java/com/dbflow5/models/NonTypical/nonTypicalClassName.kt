package com.dbflow5.models.NonTypical

import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

/**
 * Tests package name capitalized, class name is lower cased.
 */
@Table
class nonTypicalClassName(@PrimaryKey var id: Int = 0)
