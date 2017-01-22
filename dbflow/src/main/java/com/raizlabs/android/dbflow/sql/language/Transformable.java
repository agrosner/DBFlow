package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.language.property.IProperty;

import java.util.List;

/**
 * Description: Provides a standard set of methods for ending a SQLite query method. These include
 * groupby, orderby, having, limit and offset.
 */
public interface Transformable<T> {

    IWhere<T> groupBy(NameAlias... nameAliases);

    IWhere<T> groupBy(IProperty... properties);

    IWhere<T> orderBy(NameAlias nameAlias, boolean ascending);

    IWhere<T> orderBy(IProperty property, boolean ascending);

    IWhere<T> orderBy(OrderBy orderBy);

    IWhere<T> limit(int count);

    IWhere<T> offset(int offset);

    IWhere<T> having(SQLCondition... conditions);

    IWhere<T> orderByAll(List<OrderBy> orderBies);
}
