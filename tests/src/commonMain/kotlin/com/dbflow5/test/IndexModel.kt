package com.dbflow5.test

import com.dbflow5.annotation.Index
import com.dbflow5.annotation.IndexGroup
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

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
data class IndexModel(
    @Index(indexGroups = [1, 2, 3]) @PrimaryKey val id: Int,
    @Index(indexGroups = [1]) val first_name: String?,
    @Index(indexGroups = [2]) val last_name: String?,
    @Index(indexGroups = [3]) val created_date: Long?,
    @Index(indexGroups = [2, 3]) val isPro: Boolean,
)
