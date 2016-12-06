package com.raizlabs.android.dbflow.test.structure

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

@Table(database = TestDatabase::class)
class ComplexModel : BaseModel() {

    @Column
    @PrimaryKey
    var name: String? = null

    @Column
    @ForeignKey(saveForeignKeyModel = true,
        references = arrayOf(ForeignKeyReference(columnName = "testmodel_id",
            foreignKeyColumnName = "name")))
    var testModel1: TestModel1? = null

    @Column
    @ForeignKey(saveForeignKeyModel = true,
        references = arrayOf(ForeignKeyReference(columnName = "mapmodel_id",
            foreignKeyColumnName = "name")))
    var mapModelContainer: TestModel2? = null

}
