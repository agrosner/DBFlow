package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class, primaryKeyConflict = ConflictAction.REPLACE)
class PrimaryConflictModel : BaseModel() {

    @PrimaryKey(autoincrement = true)
    var id: Long = 0
}
