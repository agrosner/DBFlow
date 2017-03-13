package com.raizlabs.android.dbflow.structure.autoincrement

import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class TestSingleFieldAutoIncrement : BaseModel() {

    @PrimaryKey(autoincrement = true)
    var id: Long = 0
}
