package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.structure.TestModel1
import java.util.*

@com.raizlabs.android.dbflow.annotation.Table(database = com.raizlabs.android.dbflow.TestDatabase::class)
class DefaultModel : com.raizlabs.android.dbflow.structure.TestModel1() {

    @com.raizlabs.android.dbflow.annotation.Column(defaultValue = "55")
    var count: Int? = null

    @com.raizlabs.android.dbflow.annotation.Column(defaultValue = "this is")
    var test: String? = null

    @com.raizlabs.android.dbflow.annotation.Column(defaultValue = "1000L")
    var date: java.util.Date? = null

    @JvmSynthetic
    @com.raizlabs.android.dbflow.annotation.Column(defaultValue = "1")
    var aBoolean: Boolean? = null

    @com.raizlabs.android.dbflow.annotation.Column(defaultValue = "\"\"")
    var emptyString: String? = null

}
