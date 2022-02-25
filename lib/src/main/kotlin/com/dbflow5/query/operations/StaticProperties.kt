package com.dbflow5.query.operations

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.query.NameAlias


inline fun <reified ValueType, Table : Any> DBRepresentable<Table>.stringLiteral(
    stringRepresentation: String
):
    PropertyStart<ValueType, Table> =
    property(
        nameAlias = NameAlias.rawBuilder(stringRepresentation).build(),
        valueConverter = inferValueConverter(),
    )


/**
 * For FTS tables, "docid" is allowed as an alias along with the usual "rowid", "oid" and "_oid_" identifiers.
 * Attempting to insert or update a row with a docid value that already exists in the table is
 * an error, just as it would be with an ordinary SQLite table.
 * There is one other subtle difference between "docid" and the normal SQLite aliases for the rowid column.
 * Normally, if an INSERT or UPDATE statement assigns discrete values to two or more aliases of the rowid column, SQLite writes the rightmost of such values specified in the INSERT or UPDATE statement to the database. However, assigning a non-NULL value to both the "docid" and one or more of the SQLite rowid aliases when inserting or updating an FTS table is considered an error. See below for an example.
 */
inline fun <reified Table : Any> DBRepresentable<Table>.docId(): PropertyStart<Int, Table> =
    stringLiteral("docid")
