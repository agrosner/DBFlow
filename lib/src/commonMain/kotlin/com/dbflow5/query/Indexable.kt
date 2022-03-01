package com.dbflow5.query

import com.dbflow5.query.operations.IndexProperty
import com.dbflow5.sql.Query

interface Indexable<Table : Any> : Query {

    infix fun indexedBy(indexProperty: IndexProperty<Table>): IndexedBy<Table> =
        IndexedByImpl(this, indexProperty)
}
