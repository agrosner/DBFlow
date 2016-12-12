package com.raizlabs.android.dbflow.test.structure

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.test.TestDatabase

@Table(database = TestDatabase::class)
class TestModel2 : TestModel1() {

    @Column(name = "model_order")
    var order: Int = 0
}
