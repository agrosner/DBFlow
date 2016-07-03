package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Provides a standard set of methods for ending a SQLite query method. These include
 * groupby, orderby, having, limit and offset.
 */
public interface Transformable<T extends Model> {

    Where<T> groupBy(NameAlias... nameAliases);

    Where<T> groupBy(IProperty... properties);

    Where<T> orderBy(NameAlias nameAlias, boolean ascending);

    Where<T> orderBy(IProperty property, boolean ascending);

    Where<T> orderBy(OrderBy orderBy);

    Where<T> limit(int count);

    Where<T> offset(int offset);

    Where<T> having(SQLCondition... conditions);

}
