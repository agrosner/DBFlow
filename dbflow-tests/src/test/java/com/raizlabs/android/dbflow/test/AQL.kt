package com.raizlabs.android.dbflow.test

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.sql.BoxedModel
import java.util.*

@Table(database = TestDatabase::class, name = AQL.ENDPOINT, cachingEnabled = true)
class AQL : BaseModel() {

    interface Columns {
        companion object {
            const val AQL_ID = "aql_id"
            const val AQL_NAME = "aql_name"
            const val AQL_SERVER_ID = "aql_server_id"
            const val AQL_TIMESTAMP = "aql_timestamp"
        }
    }

    @Column(name = Columns.AQL_ID)
    @PrimaryKey(autoincrement = true)
    var aql_id: Long? = null

    @Column(name = Columns.AQL_NAME)
    var aql_name: String? = null

    @Column(name = Columns.AQL_SERVER_ID)
    var server_id: Long? = null

    @Column(name = Columns.AQL_TIMESTAMP)
    var timestamp: Date? = null

    @Column
    @ForeignKey(references = arrayOf(
        ForeignKeyReference(columnName = "id1", foreignKeyColumnName = "id"),
        ForeignKeyReference(columnName = "id2", foreignKeyColumnName = "name")))
    var boxedModel: BoxedModel? = null

    companion object {

        const val ENDPOINT = "AQL"
    }
}
