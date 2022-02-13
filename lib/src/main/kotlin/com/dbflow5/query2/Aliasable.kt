package com.dbflow5.query2

/**
 * Description:
 */
interface Aliasable<Self> {

    fun `as`(
        name: String,
        /**
         * When false, alias specified will be treated as IS.
         */
        shouldAddIdentifierToAlias: Boolean = true,
    ): Self
}

infix fun <Self> Aliasable<Self>.`as`(name: String) = `as`(name, shouldAddIdentifierToAlias = true)
