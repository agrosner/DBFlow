package com.dbflow5.query.methods

/**
 * The average value of all properties within this group. The result is always a float from this statement
 * as long as there is at least one non-NULL input. The result may be NULL if there are no non-NULL columns.
 */
val avg = AllParametersMethod<Double>("AVG")

/**
 * A count of the number of times that specified properties are not NULL in a group. Leaving
 * the properties empty returns COUNT(*), which is the total number of rows in the query.
 */
val count = AllParametersMethod<Long>("COUNT")

/**
 * A string which is the concatenation of all non-NULL values of the properties.
 */
val groupConcat = AllParametersMethod<String>("GROUP_CONCAT")

/**
 * The method that represents the max of the specified columns/properties.
 *
 * This has type parameters since MAX can represent any supported data type.
 */
fun <ReturnType> max() = AllParametersMethod<ReturnType>("MAX")

/**
 * The method that represents the min of the specified columns/properties.
 *
 * This has type parameters since MIN can represent any supported data type.
 */
fun <ReturnType> min() = AllParametersMethod<ReturnType>("MIN")

/**
 * The method that represents the sum of the specified columns/properties.
 */
val sum = AllParametersMethod<Long>("SUM")

/**
 * The method that represents the total of the specified columns/properties. Same as [sum],
 * except it returns 0.0 when all inputs are NULL.
 */
val total = AllParametersMethod<Double>("TOTAL")
