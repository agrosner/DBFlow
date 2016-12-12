package com.raizlabs.android.dbflow.test.modelcontainer

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

@Table(name = "web_service", database = TestDatabase::class)
class WebService : BaseModel() {

    @Column
    @PrimaryKey(autoincrement = true)
    var pid: Long = 0

    @Column
    @ForeignKey(references = arrayOf(
        ForeignKeyReference(columnName = "pass_type_pid", foreignKeyColumnName = "pid")),
        saveForeignKeyModel = false)
    var passType: PassType? = null

    @Column(name = "service_url")
    var serviceUrl: String? = null

}