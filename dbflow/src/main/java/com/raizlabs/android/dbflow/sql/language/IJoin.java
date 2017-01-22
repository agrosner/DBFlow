package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;

/**
 * Description:
 */

public interface IJoin<TModel, TFromModel> extends Query {

    IJoin<TModel, TFromModel> as(String alias);

    IFrom<TFromModel> natural();

    IFrom<TFromModel> on(SQLCondition... onConditions);

    IFrom<TFromModel> using(IProperty... columns);
}
