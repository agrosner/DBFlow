package com.dbflow5.query.methods

import com.dbflow5.data.Blob
import com.dbflow5.query.operations.Method
import com.dbflow5.query.operations.PropertyStart
import com.dbflow5.query.operations.method
import com.dbflow5.sql.SQLiteType

fun cast(property: PropertyStart<*, *>): Cast = Cast(property)
class Cast(val property: PropertyStart<*, *>) : StandardMethod {
    override val name: String = "CAST"

    inline infix fun <reified ReturnType> `as`(sqLiteType: SQLiteType): Method<ReturnType> =
        method(
            name, property.`as`(
                sqLiteType.name,
                shouldAddIdentifierToAlias = false
            )
        )

    fun asInteger(): Method<Int> = `as`(SQLiteType.INTEGER)

    fun asReal(): Method<Double> = `as`(SQLiteType.REAL)

    fun asText(): Method<String> = `as`(SQLiteType.TEXT)

    fun asBlob(): Method<Blob> = `as`(SQLiteType.BLOB)
}