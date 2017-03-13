package com.raizlabs.android.dbflow.sql.index

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class,
    indexGroups = arrayOf(IndexGroup(name = "customIndex", unique = true)))
class IndexModel : BaseModel() {

    @Column
    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @Index
    @Column
    var name: String? = null

    @Index
    @Column
    var salary: Long = 0

}
