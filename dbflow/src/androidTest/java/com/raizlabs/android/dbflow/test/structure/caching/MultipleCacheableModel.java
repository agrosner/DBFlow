package com.raizlabs.android.dbflow.test.structure.caching;

import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.cache.IMultiKeyCacheModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(database = TestDatabase.class, cachingEnabled = true)
public class MultipleCacheableModel extends BaseModel implements IMultiKeyCacheModel<String> {

    @PrimaryKey
    double latitude;

    @PrimaryKey
    double longitude;

    @Override
    public String getCachingKey() {
        return "(" + latitude + "," + longitude + ")";
    }
}
