package com.raizlabs.android.dbflow.test.structure.caching;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.cache.BaseCacheableModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(databaseName = TestDatabase.NAME)
public class CacheableModel2 extends BaseCacheableModel {

    @Column
    @PrimaryKey
    int id;
}
