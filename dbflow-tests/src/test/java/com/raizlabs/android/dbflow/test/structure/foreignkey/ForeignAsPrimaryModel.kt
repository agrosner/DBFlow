package com.raizlabs.android.dbflow.test.structure.foreignkey

import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase
import com.raizlabs.android.dbflow.test.sql.BoxedModel
import com.raizlabs.android.dbflow.test.structure.TestModel1

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class ForeignAsPrimaryModel : BaseModel() {

    @PrimaryKey
    var id: Long = 0

    @ForeignKey
    @PrimaryKey
    var testModel1: TestModel1? = null

    @ForeignKey
    @PrimaryKey
    var boxedModel: BoxedModel? = null

    @ForeignKey
    @PrimaryKey
    var boxedModelForeignKeyContainer: BoxedModel? = null
}
