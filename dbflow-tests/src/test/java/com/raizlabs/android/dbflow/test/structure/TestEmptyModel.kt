package com.raizlabs.android.dbflow.test.structure

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ColumnIgnore
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

/**
 * Description: Tests to ensure the allFields flag works as expected.
 */
@Table(database = TestDatabase::class, allFields = true)
class TestEmptyModel : BaseModel() {

    @Column
    @PrimaryKey
    var name: String? = null

    var count: Int = 0

    @JvmSynthetic
    var truth: Boolean = false

    internal val finalName = ""

    @ColumnIgnore
    private val hidden: Int = 0

    companion object {

        var COUNT: Int = 0
    }
}
