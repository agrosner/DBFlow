package com.dbflow5.query2.operations

import com.dbflow5.sql.Query

/**
 * Description:
 */
sealed class Operation(val value: String) : Query {

    override val query: String = value

    object Empty : Operation("")

    object WildCard : Operation("?")
    object Comma : Operation(",")

    object Equals : Operation("=")
    object NotEquals : Operation("!=")

    /**
     * String concatenation
     */
    object Concatenate : Operation("||")
    object Plus : Operation("+")
    object Minus : Operation("-")
    object Division : Operation("/")
    object Times : Operation("*")
    object Rem : Operation("%")

    /**
     * If something is LIKE another (a case insensitive search).
     * There are two wildcards: % and _
     * % represents [0,many) numbers or characters.
     * The _ represents a single number or character.
     */
    object Like : Operation("LIKE")

    /**
     * If the WHERE clause of the SELECT statement contains a sub-clause of the form "<column> MATCH ?",
     * FTS is able to use the built-in full-text index to restrict the search to those documents
     * that match the full-text query string specified as the right-hand operand of the MATCH clause.
     */
    object Match : Operation("MATCH")

    /**
     * If something is NOT LIKE another (a case insensitive search).
     * There are two wildcards: % and _
     * % represents [0,many) numbers or characters.
     * The _ represents a single number or character.
     */
    object NotLike : Operation("NOT LIKE")

    /**
     * If something is case sensitive like another.
     * It must be a string to escape it properly.
     * There are two wildcards: * and ?
     * * represents [0,many) numbers or characters.
     * The ? represents a single number or character
     */
    object Glob : Operation("GLOB")

    object GreaterThan : Operation(">")
    object GreaterThanOrEquals : Operation(">=")
    object LessThan : Operation("<")
    object LessThanOrEquals : Operation("<=")

    object Between : Operation("BETWEEN")
    object And : Operation("AND")
    object Or : Operation("OR")

    object IsNotNull : Operation("IS NOT NULL")
    object IsNull : Operation("IS NULL")

    object In : Operation("IN")
    object NotIn : Operation("NOT IN")
}