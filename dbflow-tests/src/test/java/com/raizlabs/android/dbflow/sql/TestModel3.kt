package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.structure.TestModel1

/**
 * Description:
 */
@Table(database = TestDatabase::class, name = "TestModel32")
class TestModel3 : TestModel1() {
    @Column
    var type: String? = null
}
