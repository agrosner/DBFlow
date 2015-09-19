package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.Property;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description: Specifies a SQLite JOIN statement
 */
public class Join<ModelClass extends Model, FromClass extends Model> implements Query {

    /**
     * The specific type of JOIN that is used.
     */
    public enum JoinType {

        LEFT,

        OUTER,

        INNER,

        CROSS,
    }

    /**
     * The table to JOIN on
     */
    private Class<ModelClass> table;

    /**
     * The type of JOIN to use
     */
    private JoinType type;

    /**
     * The FROM statement that prefixes this statement.
     */
    private From<FromClass> from;

    /**
     * The alias to name the JOIN
     */
    private NameAlias alias;

    /**
     * The ON conditions
     */
    private ConditionGroup on;

    /**
     * What columns to use.
     */
    private List<Property> using = new ArrayList<>();

    /**
     * If it is a natural JOIN.
     */
    private boolean isNatural = false;

    Join(From<FromClass> from, Class<ModelClass> table, JoinType joinType) {
        this.from = from;
        this.table = table;
        type = joinType;
        alias = new NameAlias(FlowManager.getTableName(table));
    }

    /**
     * Specifies if the JOIN has a name it should be called.
     *
     * @param alias The name to give it
     * @return This instance
     */
    public Join<ModelClass, FromClass> as(String alias) {
        this.alias.as(alias);
        return this;
    }

    /**
     * Specifies that this JOIN is a natural JOIN
     *
     * @return The FROM that this JOIN came from.
     */
    public From<FromClass> natural() {
        isNatural = true;
        return from;
    }

    /**
     * Specify the conditions that the JOIN is on
     *
     * @param onConditions The conditions it is on
     * @return The FROM that this JOIN came from
     */
    public From<FromClass> on(Condition... onConditions) {
        on = new ConditionGroup();
        on.andAll(onConditions);
        return from;
    }

    /**
     * The USING statement of the JOIN
     *
     * @param columns THe columns to use
     * @return The FROM that this JOIN came from
     */
    public From<FromClass> using(Property... columns) {
        Collections.addAll(using, columns);
        return from;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();

        if (isNatural) {
            queryBuilder.append("NATURAL ");
        }

        queryBuilder.append(type.toString()).appendSpace();

        queryBuilder.append("JOIN")
                .appendSpace()
                .append(alias.getDefinition())
                .appendSpace();

        if (on != null) {
            queryBuilder.append("ON")
                    .appendSpace()
                    .append(on.getQuery())
                    .appendSpace();
        } else if (using != null) {
            queryBuilder.append("USING (")
                    .appendArray(using)
                    .append(")").appendSpace();
        }
        return queryBuilder.getQuery();
    }

}
