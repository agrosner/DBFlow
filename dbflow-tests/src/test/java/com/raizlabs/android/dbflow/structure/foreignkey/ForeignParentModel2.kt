package com.raizlabs.android.dbflow.structure.foreignkey

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class ForeignParentModel2 : ForeignParentModel() {

    @Column
    @PrimaryKey
    var anotherName: String? = null
}
