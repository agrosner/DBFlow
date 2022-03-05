package com.dbflow5.adapter

import com.dbflow5.annotation.opts.DelicateDBFlowApi
import com.dbflow5.database.DatabaseWrapper
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
fun <Table : Any> WritableDBRepresentable<Table>.create(db: DatabaseWrapper) =
    UnitResultFactory.run { db.createResult(creationSQL.query) }

@DelicateDBFlowApi
fun <Table : Any> WritableDBRepresentable<Table>.drop(db: DatabaseWrapper) =
    UnitResultFactory.run { db.createResult(dropSQL.query) }
