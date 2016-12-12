package com.raizlabs.android.dbflow.test.container

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

@Table(database = TestDatabase::class, useBooleanGetterSetters = true)
open class AutoIncrementContainer : BaseModel() {

    @Column
    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @Column
    var name: String? = null

    @Column
    var a_id: Long = 0

    @Column
    var isABoolean: Boolean = false

    @Column
    var isEnabled: Boolean = false
}
