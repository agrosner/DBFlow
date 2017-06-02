package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.property.IProperty;

import java.util.List;

/**
 * Description: Provides a standard set of methods for ending a SQLite query method. These include
 * groupby, orderby, having, limit and offset.
 */
public interface Transformable<T> {

    @NonNull
    Where<T> groupBy(NameAlias... nameAliases);

    @NonNull
    Where<T> groupBy(IProperty... properties);

    @NonNull
    Where<T> orderBy(@NonNull NameAlias nameAlias, boolean ascending);

    @NonNull
    Where<T> orderBy(@NonNull IProperty property, boolean ascending);

    @NonNull
    Where<T> orderBy(@NonNull OrderBy orderBy);

    @NonNull
    Where<T> limit(int count);

    @NonNull
    Where<T> offset(int offset);

    @NonNull
    Where<T> having(SQLOperator... conditions);

    @NonNull
    Where<T> orderByAll(@NonNull List<OrderBy> orderBies);
}
