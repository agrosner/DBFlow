package com.grosner.dbflow.sql;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: The basic interface for something that has a piece of a query.
 */
public interface Query {

    /**
     * Return the SQL query statement here
     *
     * @return
     */
    public String getQuery();
}
