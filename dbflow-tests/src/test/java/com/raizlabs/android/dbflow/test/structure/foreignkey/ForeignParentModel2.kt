package com.raizlabs.android.dbflow.test.structure.foreignkey

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.test.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class ForeignParentModel2 : ForeignParentModel() {

    @Column
    @PrimaryKey
    var anotherName: String? = null
}
