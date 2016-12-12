package com.raizlabs.android.dbflow.test.sql

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class, useBooleanGetterSetters = true)
class PrivateModelTest : BaseModel() {

    @Column
    @PrimaryKey
    var name: String? = null

    @Column(getterName = "getThisName", setterName = "setThisName")
    var thisName: String? = null

    @Column
    var isOpen: Boolean = false
}
