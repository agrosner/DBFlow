package com.dbflow5.adapter

import com.dbflow5.annotation.opts.DelicateDBFlowApi
import com.dbflow5.database.DatabaseConnection
import com.dbflow5.query.UnitResultFactory
import com.dbflow5.query.operations.Property

/**
 * Represents an adapter that has a name in DB.
 */
interface DBRepresentable<DBType : Any> : QueryRepresentable<DBType> {

    val name: String

    val createWithDatabase: Boolean

    val creationSQL: CompilableQuery

    val dropSQL: CompilableQuery

    fun getProperty(columnName: String): Property<*, DBType>
}

/**
 * Enables mutation queries.
 */
interface WritableDBRepresentable<DBType : Any> : DBRepresentable<DBType>

@DelicateDBFlowApi
fun <Table : Any> DBRepresentable<Table>.create(db: DatabaseConnection) =
    UnitResultFactory.run { db.createResult(creationSQL.query) }

@DelicateDBFlowApi
fun <Table : Any> DBRepresentable<Table>.drop(db: DatabaseConnection) =
    UnitResultFactory.run { db.createResult(dropSQL.query) }
