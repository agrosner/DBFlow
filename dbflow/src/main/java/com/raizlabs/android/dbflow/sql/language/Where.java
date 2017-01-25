package com.raizlabs.android.dbflow.sql.language;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.provider.ContentProvider;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description: Defines the SQL WHERE statement of the query.
 */
public class Where<TModel> extends BaseModelQueriable<TModel>
    implements IWhere<TModel>, ModelQueriable<TModel> {

    private static final int VALUE_UNSET = -1;

    /**
     * The first chunk of the SQL statement before this query.
     */
    private final WhereBase<TModel> whereBase;

    /**
     * Helps to build the where statement easily
     */
    private ConditionGroup conditionGroup;

    private final List<NameAlias> groupByList = new ArrayList<>();

    private final List<OrderBy> orderByList = new ArrayList<>();

    /**
     * The SQL HAVING statement
     */
    private ConditionGroup havingGroup;

    private int limit = VALUE_UNSET;
    private int offset = VALUE_UNSET;

    /**
     * Constructs this class with the specified {@link com.raizlabs.android.dbflow.config.FlowManager}
     * and {@link From} chunk
     *
     * @param whereBase The FROM or SET statement chunk
     */
    public Where(WhereBase<TModel> whereBase, SQLCondition... conditions) {
        super(whereBase.getTable());
        this.whereBase = whereBase;
        conditionGroup = new ConditionGroup();
        havingGroup = new ConditionGroup();

        conditionGroup.andAll(conditions);
    }

    /**
     * Joins the {@link SQLCondition} by the prefix of "AND" (unless its the first condition).
     */
    @Override
    public Where<TModel> and(SQLCondition condition) {
        conditionGroup.and(condition);
        return this;
    }

    /**
     * Joins the {@link SQLCondition} by the prefix of "OR" (unless its the first condition).
     */
    @Override
    public Where<TModel> or(SQLCondition condition) {
        conditionGroup.or(condition);
        return this;
    }

    /**
     * Joins all of the {@link SQLCondition} by the prefix of "AND" (unless its the first condition).
     */
    @Override
    public Where<TModel> andAll(List<SQLCondition> conditions) {
        conditionGroup.andAll(conditions);
        return this;
    }

    /**
     * Joins all of the {@link SQLCondition} by the prefix of "AND" (unless its the first condition).
     */
    @Override
    public Where<TModel> andAll(SQLCondition... conditions) {
        conditionGroup.andAll(conditions);
        return this;
    }

    @Override
    public Where<TModel> groupBy(NameAlias... columns) {
        Collections.addAll(groupByList, columns);
        return this;
    }

    @Override
    public Where<TModel> groupBy(IProperty... properties) {
        for (IProperty property : properties) {
            groupByList.add(property.getNameAlias());
        }
        return this;
    }

    /**
     * Defines a SQL HAVING statement without the HAVING.
     *
     * @param conditions The array of {@link SQLCondition}
     * @return
     */
    @Override
    public Where<TModel> having(SQLCondition... conditions) {
        havingGroup.andAll(conditions);
        return this;
    }

    @Override
    public Where<TModel> orderBy(NameAlias nameAlias, boolean ascending) {
        orderByList.add(new OrderBy(nameAlias, ascending));
        return this;
    }

    @Override
    public Where<TModel> orderBy(IProperty property, boolean ascending) {
        orderByList.add(new OrderBy(property.getNameAlias(), ascending));
        return this;
    }

    @Override
    public Where<TModel> orderBy(OrderBy orderBy) {
        orderByList.add(orderBy);
        return this;
    }

    /**
     * For use in {@link ContentProvider} generation. Appends all ORDER BY here.
     *
     * @param orderBies The order by.
     * @return this instance.
     */
    @Override
    public Where<TModel> orderByAll(List<OrderBy> orderBies) {
        if (orderBies != null) {
            orderByList.addAll(orderBies);
        }
        return this;
    }

    @Override
    public Where<TModel> limit(int count) {
        this.limit = count;
        return this;
    }

    @Override
    public Where<TModel> offset(int offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Specify that we use an EXISTS statement for this Where class.
     *
     * @param where The query to use in the EXISTS clause. Such as SELECT * FROM `MyTable` WHERE ... etc.
     * @return This where with an EXISTS clause.
     */
    @Override
    public Where<TModel> exists(@NonNull IWhere where) {
        conditionGroup.and(new ExistenceCondition()
            .where(where));
        return this;
    }

    @Override
    public BaseModel.Action getPrimaryAction() {
        return whereBase.getPrimaryAction();
    }

    @Override
    public String getQuery() {
        String fromQuery = whereBase.getQuery().trim();
        QueryBuilder queryBuilder = new QueryBuilder().append(fromQuery).appendSpace()
            .appendQualifier("WHERE", conditionGroup.getQuery())
            .appendQualifier("GROUP BY", QueryBuilder.join(",", groupByList))
            .appendQualifier("HAVING", havingGroup.getQuery())
            .appendQualifier("ORDER BY", QueryBuilder.join(",", orderByList));

        if (limit > VALUE_UNSET) {
            queryBuilder.appendQualifier("LIMIT", String.valueOf(limit));
        }
        if (offset > VALUE_UNSET) {
            queryBuilder.appendQualifier("OFFSET", String.valueOf(offset));
        }

        return queryBuilder.getQuery();
    }

    /**
     * @return the result of the query as a {@link Cursor}.
     */
    @Override
    public Cursor query(DatabaseWrapper wrapper) {
        // Query the sql here
        Cursor cursor;
        if (whereBase.getQueryBuilderBase() instanceof Select) {
            cursor = wrapper.rawQuery(getQuery(), null);
        } else {
            cursor = super.query(wrapper);
        }

        return cursor;
    }

    @Override
    public Cursor query() {
        return query(FlowManager.getDatabaseForTable(getTable()).getWritableDatabase());
    }

    /**
     * Queries for all of the results this statement returns from a DB cursor in the form of the {@link TModel}
     *
     * @return All of the entries in the DB converted into {@link TModel}
     */
    @Override
    public List<TModel> queryList() {
        checkSelect("query");
        return super.queryList();
    }

    public WhereBase<TModel> getWhereBase() {
        return whereBase;
    }

    protected void checkSelect(String methodName) {
        if (!(whereBase.getQueryBuilderBase() instanceof Select)) {
            throw new IllegalArgumentException("Please use " + methodName + "(). The beginning is not a Select");
        }
    }

    /**
     * Queries and returns only the first {@link TModel} result from the DB. Will enforce a limit of 1 item
     * returned from the database.
     *
     * @return The first result of this query. Note: this query forces a limit of 1 from the database.
     */
    @Override
    public TModel querySingle() {
        checkSelect("query");
        limit(1);
        return super.querySingle();
    }

}
