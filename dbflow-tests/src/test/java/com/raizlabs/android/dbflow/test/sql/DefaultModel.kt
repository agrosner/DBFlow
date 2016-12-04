package com.raizlabs.android.dbflow.test.sql

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.test.TestDatabase
import com.raizlabs.android.dbflow.test.structure.TestModel1
import java.util.*

@Table(database = TestDatabase::class)
class DefaultModel : TestModel1() {

    @Column(defaultValue = "55")
    var count: Int? = null

    @Column(defaultValue = "this is")
    var test: String? = null

    @Column(defaultValue = "1000L")
    var date: Date? = null

    @JvmSynthetic
    @Column(defaultValue = "1")
    var aBoolean: Boolean? = null

    @Column(defaultValue = "\"\"")
    var emptyString: String? = null

}
