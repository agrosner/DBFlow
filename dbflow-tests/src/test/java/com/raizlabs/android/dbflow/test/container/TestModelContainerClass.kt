package com.raizlabs.android.dbflow.test.container

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.test.TestDatabase
import com.raizlabs.android.dbflow.test.structure.TestModel1

/**
 * Tests to ensure model containers
 */
@Table(name = "TestModelContainer", database = TestDatabase::class)
class TestModelContainerClass : TestModel1() {

    @Column
    @PrimaryKey
    var party_type: String? = null

    @Column
    var count: Int = 0

    @Column
    var party_name: String? = null

    @Column
    var isHappy: Boolean = false

    @Column
    @ForeignKey(saveForeignKeyModel = true, onDelete = ForeignKeyAction.CASCADE,
        onUpdate = ForeignKeyAction.CASCADE)
    var testModel: ParentModel? = null
}
