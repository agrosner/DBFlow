package com.raizlabs.android.dbflow.structure.provider;

import android.net.Uri;

import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: A base interface for Models that are connected to providers.
 */
public interface ModelProvider<TableClass extends Model> {

    public void load(ConditionQueryBuilder<TableClass> whereConditions,
                     String orderBy, String... columns);

    public Uri getDeleteUri();

    public Uri getInsertUri();

    public Uri getUpdateUri();

    public Uri getQueryUri();
}
