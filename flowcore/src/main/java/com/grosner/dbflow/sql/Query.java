package com.grosner.dbflow.sql;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: The basic interface for something that has a piece of a query.
 */
public interface Query {

    /**
     * @return the SQL query statement here
     */
    public String getQuery();
}
