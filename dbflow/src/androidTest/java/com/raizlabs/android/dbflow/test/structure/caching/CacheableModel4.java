package com.raizlabs.android.dbflow.test.structure.caching;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.cache.BaseCacheableModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description: Cacheable model.
 */
@Table(database = TestDatabase.class)
public class CacheableModel4 extends BaseCacheableModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;
}
