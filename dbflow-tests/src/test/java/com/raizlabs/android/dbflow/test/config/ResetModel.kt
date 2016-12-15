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
@Table(database = TestDatabase::class)
class ResetModel : BaseModel() {

    @Column
    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @Column
    var name: String? = null
}
