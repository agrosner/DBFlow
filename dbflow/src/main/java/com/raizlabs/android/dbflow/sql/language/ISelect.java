package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.Query;

public interface ISelect extends Query {

    @NonNull
    ISelect distinct();

    @NonNull
    String toString();

    @NonNull
    <TModel> IFrom<TModel> from(Class<TModel> table);
}
