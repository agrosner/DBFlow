package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.annotation.Unique
import com.raizlabs.android.dbflow.annotation.UniqueGroup
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

@Table(database = TestDatabase::class, uniqueColumnGroups = arrayOf(UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.FAIL), UniqueGroup(groupNumber = 2, uniqueConflict = ConflictAction.ROLLBACK)))
class UniqueModel2 : BaseModel() {

    @Column
    @PrimaryKey
    @Unique(unique = false, uniqueGroups = intArrayOf(1, 2))
    var name: String? = null

    @Column
    @Unique(unique = false, uniqueGroups = intArrayOf(1))
    var number: String? = null

    @Column
    @Unique(unique = false, uniqueGroups = intArrayOf(2))
    var address: String? = null

}
