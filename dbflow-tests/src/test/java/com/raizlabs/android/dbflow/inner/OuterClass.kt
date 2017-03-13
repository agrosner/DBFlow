package com.raizlabs.android.dbflow.inner

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:
 */
class OuterClass {

    @Table(database = TestDatabase::class)
    class InnerClass : BaseModel() {

        @PrimaryKey
        var id: Long = 0

        @Column
        var column: String? = null
    }
}
