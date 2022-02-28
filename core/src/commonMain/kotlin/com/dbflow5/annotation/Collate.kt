package com.dbflow5.annotation

import com.dbflow5.sql.Query

/**
 * Represents a SQL Collate method for comparing string columns.
 */
enum class Collate(
    val value: String,
) : Query {

    /**
     * Tells the table creation and condition that we wont use collation
     */
    None(""),

    /**
     * Compares string data normally
     */
    Binary("BINARY"),

    /**
     * Ignores case for the 26 upper case characters of ASCII are folded to lower case equivalents.
     * Does not attempt to fold UTF so be careful!
     */
    NoCase("NOCASE"),

    /**
     * Trims trailing space characters before performing comparison.
     */
    RTrim("RTRIM"),

    /**
     * Takes the current locale data into account.
     */
    Localized("LOCALIZED"),

    /**
     * The Unicode Collation Algorithm and not tailored to the current locale.
     */
    Unicode("UNICODE");

    override val query: String
        get() = "COLLATE $value"
}
