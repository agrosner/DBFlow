package com.dbflow5.query2

import com.dbflow5.query.property.IndexProperty
import com.dbflow5.sql.Query

interface Indexable<Table : Any> : Query {

    infix fun indexedBy(indexProperty: IndexProperty<Table>): IndexedBy<Table> =
        IndexedByImpl(this, indexProperty)
}
