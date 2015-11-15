package com.raizlabs.android.dbflow.test.structure.caching;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.cache.BaseCacheableModel;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.test.TestDatabase;

@Table(database = TestDatabase.class)
public class CacheableModel3 extends BaseCacheableModel {

    @Column
    @PrimaryKey
    String cache_id;

    @Column
    int number;

    @Override
    protected ModelCache<? extends BaseCacheableModel, ?> getBackingCache() {
        return new SimpleMapCache<>();
    }
}
