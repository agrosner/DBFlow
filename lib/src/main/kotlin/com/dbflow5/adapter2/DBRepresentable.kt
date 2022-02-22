package com.dbflow5.adapter2

/**
 * Represents an adapter that has a name in DB.
 */
interface DBRepresentable {

    val name: String

    val creationSQL: CompilableQuery
}