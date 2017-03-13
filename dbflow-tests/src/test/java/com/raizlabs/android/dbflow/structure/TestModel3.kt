package com.raizlabs.android.dbflow.structure

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.*
import java.sql.Date

@Table(database = TestDatabase::class)
class TestModel3 : BaseModel() {

    @Column
    @PrimaryKey
    var name: String? = null

    @Column(length = 5, collate = Collate.BINARY)
    var order: Int = 0

    @Column(collate = Collate.NOCASE)
    var date: Date? = null

    @Column
    @ForeignKey(references = arrayOf(ForeignKeyReference(columnName = "testAI_name",
        foreignKeyColumnName = "name")))
    var testAutoIncrement: TestModel1? = null
}
