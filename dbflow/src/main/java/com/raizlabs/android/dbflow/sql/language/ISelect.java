package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;

public interface ISelect extends Query {

    ISelect distinct();

    String toString();

    <TModel> IFrom<TModel> from(Class<TModel> table);
}
