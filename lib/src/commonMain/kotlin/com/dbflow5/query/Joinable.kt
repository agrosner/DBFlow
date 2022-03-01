package com.dbflow5.query

import com.dbflow5.adapter.DBRepresentable

sealed class JoinType(val value: String) {
    /**
     * an extension of the INNER JOIN. Though SQL standard defines three types of OUTER JOINs: LEFT, RIGHT,
     * and FULL but SQLite only supports the LEFT OUTER JOIN.
     *
     *
     * The OUTER JOINs have a condition that is identical to INNER JOINs, expressed using an ON, USING, or NATURAL keyword.
     * The initial results table is calculated the same way. Once the primary JOIN is calculated,
     * an OUTER join will take any unjoined rows from one or both tables, pad them out with NULLs,
     * and append them to the resulting table.
     */
    object LeftOuter : JoinType("LEFT OUTER")

    /**
     * creates a new result table by combining column values of two tables (table1 and table2) based upon the join-predicate.
     * The query compares each row of table1 with each row of table2 to find all pairs of rows which satisfy the join-predicate.
     * When the join-predicate is satisfied, column values for each matched pair of rows of A and B are combined into a result row
     */
    object Inner : JoinType("INNER")

    /**
     * matches every row of the first table with every row of the second table. If the input tables
     * have x and y columns, respectively, the resulting table will have x*y columns.
     * Because CROSS JOINs have the potential to generate extremely large tables,
     * care must be taken to only use them when appropriate.
     */
    object Cross : JoinType("CROSS")

    /**
     * a join that performs the same task as an INNER or LEFT JOIN, in which the ON or USING
     * clause refers to all columns that the tables to be joined have in common.
     */
    object Natural : JoinType("NATURAL")
}

interface Joinable<OriginalTable : Any, Result> {

    fun <JoinTable : Any> join(
        adapter: DBRepresentable<JoinTable>,
        joinType: JoinType,
    ): Join<OriginalTable, JoinTable, Result>

    fun <JoinTable : Any> join(
        hasAdapter: HasAdapter<JoinTable, DBRepresentable<JoinTable>>,
        joinType: JoinType,
    ): Join<OriginalTable, JoinTable, Result>
}

infix fun <OriginalTable : Any, JoinTable : Any, Result>
    Joinable<OriginalTable, Result>.crossJoin(
    adapter: DBRepresentable<JoinTable>
) = join(adapter, JoinType.Cross)

infix fun <OriginalTable : Any, JoinTable : Any, Result>
    Joinable<OriginalTable, Result>.crossJoin(
    hasAdapter: HasAdapter<JoinTable, DBRepresentable<JoinTable>>,
) = join(hasAdapter, JoinType.Cross)

infix fun <OriginalTable : Any, JoinTable : Any, Result>
    Joinable<OriginalTable, Result>.innerJoin(
    adapter: DBRepresentable<JoinTable>
) = join(adapter, JoinType.Inner)

infix fun <OriginalTable : Any, JoinTable : Any, Result>
    Joinable<OriginalTable, Result>.innerJoin(
    hasAdapter: HasAdapter<JoinTable, DBRepresentable<JoinTable>>,
) = join(hasAdapter, JoinType.Inner)

infix fun <OriginalTable : Any, JoinTable : Any, Result>
    Joinable<OriginalTable, Result>.leftOuterJoin(
    adapter: DBRepresentable<JoinTable>
) = join(adapter, JoinType.LeftOuter)

infix fun <OriginalTable : Any, JoinTable : Any, Result>
    Joinable<OriginalTable, Result>.leftOuterJoin(
    hasAdapter: HasAdapter<JoinTable, DBRepresentable<JoinTable>>,
) = join(hasAdapter, JoinType.LeftOuter)

/**
 * TODO: use separate interface type?
 */
infix fun <OriginalTable : Any, JoinTable : Any, Result>
    Joinable<OriginalTable, Result>.naturalJoin(
    adapter: DBRepresentable<JoinTable>
) = join(adapter, JoinType.Natural).end()

infix fun <OriginalTable : Any, JoinTable : Any, Result>
    Joinable<OriginalTable, Result>.naturalJoin(
    hasAdapter: HasAdapter<JoinTable, DBRepresentable<JoinTable>>,
) = join(hasAdapter, JoinType.Natural)

