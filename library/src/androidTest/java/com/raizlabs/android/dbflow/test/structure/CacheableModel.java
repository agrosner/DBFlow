package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.cache.BaseCacheableModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(databaseName = TestDatabase.NAME)
public class CacheableModel extends BaseCacheableModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String name;

    @Override
    public int getCacheSize() {
        return 1000;
    }
}
