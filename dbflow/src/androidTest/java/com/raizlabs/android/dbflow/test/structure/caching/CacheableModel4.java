package com.raizlabs.android.dbflow.test.structure.caching;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description: Cacheable model.
 */
@Table(database = TestDatabase.class, cachingEnabled = true)
public class CacheableModel4 extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;
}
