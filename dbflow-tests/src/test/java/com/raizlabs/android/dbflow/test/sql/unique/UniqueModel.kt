package com.raizlabs.android.dbflow.test.sql.unique

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.annotation.Unique
import com.raizlabs.android.dbflow.annotation.UniqueGroup
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class, uniqueColumnGroups = arrayOf(UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.IGNORE), UniqueGroup(groupNumber = 2, uniqueConflict = ConflictAction.REPLACE)))
class UniqueModel : BaseModel() {

    @Column
    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @Column
    @Unique(uniqueGroups = intArrayOf(1), onUniqueConflict = ConflictAction.REPLACE)
    var uniqueName: String? = null

    @Column
    @Unique(uniqueGroups = intArrayOf(2), onUniqueConflict = ConflictAction.IGNORE)
    var anotherUnique: String? = null

    @Column
    @Unique(uniqueGroups = intArrayOf(1, 2))
    var sharedUnique: String? = null
}
