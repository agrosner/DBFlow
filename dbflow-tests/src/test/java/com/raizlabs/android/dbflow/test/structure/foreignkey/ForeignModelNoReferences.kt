package com.raizlabs.android.dbflow.test.structure.foreignkey

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.test.TestDatabase
import com.raizlabs.android.dbflow.test.structure.TestModel1

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class ForeignModelNoReferences : TestModel1() {

    @Column
    @ForeignKey
    var parentModel: ForeignParentModel? = null

    @Column
    @ForeignKey(tableClass = ForeignParentModel::class)
    var parentName: String? = null

    @Column
    @ForeignKey
    var parentModel2: ForeignParentModel2? = null
}
