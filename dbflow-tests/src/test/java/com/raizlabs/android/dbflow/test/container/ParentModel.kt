package com.raizlabs.android.dbflow.test.container

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.test.TestDatabase
import com.raizlabs.android.dbflow.test.structure.TestModel1

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class ParentModel : TestModel1() {

    @Column
    @PrimaryKey
    var type: String? = null
}
