@file:Suppress("PackageName", "ClassName")

package com.dbflow5.test.NonTypical

import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

/**
 * Tests package name capitalized, class name is lower cased.
 */
@Table
data class nonTypicalClassName(@PrimaryKey val id: Int)
