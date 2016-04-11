package com.raizlabs.android.dbflow.test.structure.caching;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.MultiCacheField;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.cache.IMultiKeyCacheConverter;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

/**
 * Description:
 */
@Table(database = TestDatabase.class, cachingEnabled = true)
public class MultipleCacheableModel extends BaseModel {

    @MultiCacheField
    public static final IMultiKeyCacheConverter<String> multiKeyCacheModel = new IMultiKeyCacheConverter<String>() {

        @Override
        @NonNull
        public String getCachingKey(@NonNull Object[] values) {
            return "(" + values[0] + "," + values[1] + ")";
        }
    };

    @PrimaryKey
    double latitude;

    @PrimaryKey
    double longitude;

    @ForeignKey(references = {@ForeignKeyReference(columnName = "associatedModel",
            columnType = String.class, foreignKeyColumnName = "name", referencedFieldIsPackagePrivate = true)})
    TestModel1 associatedModel;

}
