package com.raizlabs.android.dbflow.container

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.structure.TestModel1

@Table(database = TestDatabase::class)
class ForeignInteractionModel : TestModel1() {

    @Column
    @ForeignKey(onDelete = ForeignKeyAction.CASCADE,
            onUpdate = ForeignKeyAction.CASCADE)
    var testModel1: ParentModel? = null

}
