package com.raizlabs.android.dbflow.test.structure.foreignkey

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.test.TestDatabase
import com.raizlabs.android.dbflow.test.structure.TestModel1

@Table(database = TestDatabase::class)
class ForeignModel : TestModel1() {
    @Column
    @ForeignKey(references = arrayOf(ForeignKeyReference(columnName = "testmodel_id",
        foreignKeyColumnName = "name")))
    var testModel1: ForeignParentModel? = null
}
