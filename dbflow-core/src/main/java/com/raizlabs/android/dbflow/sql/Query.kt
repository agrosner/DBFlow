package com.raizlabs.android.dbflow.sql

/**
 * Description: The basic interface for something that has a piece of a query.
 */
interface Query {

    /**
     * @return the SQL query statement here
     */
    val query: String
}
