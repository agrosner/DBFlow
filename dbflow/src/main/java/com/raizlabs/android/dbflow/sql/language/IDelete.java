package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;

/**
 * Description:
 */

public interface IDelete extends Query {

    <TModel> IFrom<TModel> from(Class<TModel> table);
}
