package com.raizlabs.android.dbflow.test.sql

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class, insertConflict = ConflictAction.ABORT)
class InsertConflictModel : BaseModel() {

    @PrimaryKey
    var id: Long = 0

    @Column
    var name: String? = null
}
