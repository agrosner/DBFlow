package com.dbflow5.models

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.Index
import com.dbflow5.annotation.IndexGroup
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import java.util.*

/**
 * Description:
 */

@Table(
    indexGroups = [
        IndexGroup(number = 1, name = "firstIndex"),
        IndexGroup(number = 2, name = "secondIndex"),
        IndexGroup(number = 3, name = "thirdIndex"),
    ]
)
class IndexModel {
    @Index(indexGroups = [1, 2, 3])
    @PrimaryKey
    var id: Int = 0

    @Index(indexGroups = [1])
    @Column
    var first_name: String? = null

    @Index(indexGroups = [2])
    @Column
    var last_name: String? = null

    @Index(indexGroups = [3])
    @Column
    var created_date: Date? = null

    @Index(indexGroups = [2, 3])
    @Column
    var isPro: Boolean = false
}