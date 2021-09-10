package com.raizlabs.android.dbflow.sql.language;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.FlowCursor;

import java.util.List;

/**
 * Description: Combines basic transformations and query ops into a base class.
 */
public abstract class BaseTransformable<TModel> extends BaseModelQueriable<TModel> implements Transformable<TModel>,
    WhereBase<TModel> {

    /**
     * Constructs new instance of this class and is meant for subclasses only.
     *
     * @param table the table that belongs to this query.
     */
    protected BaseTransformable(Class<TModel> table) {
        super(table);
    }

    @NonNull
    public Where<TModel> where(@NonNull SQLOperator... conditions) {
        return new Where<>(this, conditions);
    }

    @Override
    public FlowCursor query() {
        return where().query();
    }

    @Override
    public FlowCursor query(@NonNull DatabaseWrapper databaseWrapper) {
        return where().query(databaseWrapper);
    }

    @NonNull
    @Override
    public Where<TModel> groupBy(NameAlias... nameAliases) {
        return where().groupBy(nameAliases);
    }

    @NonNull
    @Override
    public Where<TModel> groupBy(IProperty... properties) {
        return where().groupBy(properties);
    }

    @NonNull
    @Override
    public Where<TModel> orderBy(@NonNull NameAlias nameAlias, boolean ascending) {
        return where().orderBy(nameAlias, ascending);
    }

    @NonNull
    @Override
    public Where<TModel> orderBy(@NonNull IProperty property, boolean ascending) {
        return where().orderBy(property, ascending);
    }

    @NonNull
    @Override
    public Where<TModel> orderByAll(@NonNull List<OrderBy> orderBies) {
        return where().orderByAll(orderBies);
    }

    @NonNull
    @Override
    public Where<TModel> orderBy(@NonNull OrderBy orderBy) {
        return where().orderBy(orderBy);
    }

    @NonNull
    @Override
    public Where<TModel> limit(int count) {
        return where().limit(count);
    }

    @NonNull
    @Override
    public Where<TModel> offset(int offset) {
        return where().offset(offset);
    }

    @NonNull
    @Override
    public Where<TModel> having(SQLOperator... conditions) {
        return where().having(conditions);
    }

    @NonNull
    @Override
    public List<TModel> queryList() {
        checkSelect("query");
        return super.queryList();
    }

    @Override
    public TModel querySingle() {
        checkSelect("query");
        limit(1);
        return super.querySingle();
    }

    private void checkSelect(String methodName) {
        if (!(getQueryBuilderBase() instanceof Select)) {
            throw new IllegalArgumentException("Please use " + methodName + "(). The beginning is not a Select");
        }
    }
}
