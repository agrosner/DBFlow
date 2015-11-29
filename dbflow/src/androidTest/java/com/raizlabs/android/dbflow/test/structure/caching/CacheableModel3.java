package com.raizlabs.android.dbflow.test.structure.caching;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelCacheField;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.test.TestDatabase;

@Table(database = TestDatabase.class, cachingEnabled = true)
public class CacheableModel3 extends BaseModel {

    @ModelCacheField
    public static ModelCache<? extends Model, ?> modelCache = new SimpleMapCache<>();

    @Column
    @PrimaryKey
    String cache_id;

    @Column
    int number;

}
