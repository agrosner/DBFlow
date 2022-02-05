package com.dbflow5.query2

/**
 * Description:
 */
interface Aliasable<Self> {

    infix fun `as`(name: String): Self
}
