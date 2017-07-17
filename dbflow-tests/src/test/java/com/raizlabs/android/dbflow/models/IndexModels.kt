package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.Index
import com.raizlabs.android.dbflow.annotation.IndexGroup
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import java.util.*

/**
 * Description:
 */

@Table(database = TestDatabase::class, indexGroups = arrayOf(IndexGroup(number = 1, name = "firstIndex"),
    IndexGroup(number = 2, name = "secondIndex"),
    IndexGroup(number = 3, name = "thirdIndex")))
class IndexModel {
    @Index(indexGroups = intArrayOf(1, 2, 3))
    @PrimaryKey
    var id: Int = 0

    @Index(indexGroups = intArrayOf(1))
    @Column
    var first_name: String? = null

    @Index(indexGroups = intArrayOf(2))
    @Column
    var last_name: String? = null

    @Index(indexGroups = intArrayOf(3))
    @Column
    var created_date: Date? = null

    @Index(indexGroups = intArrayOf(2, 3))
    @Column
    var isPro: Boolean = false
}