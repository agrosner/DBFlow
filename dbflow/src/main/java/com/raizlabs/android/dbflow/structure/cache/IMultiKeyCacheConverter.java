package com.raizlabs.android.dbflow.structure.cache;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: This interface allows for {@link Model} to have multiple primary keys in a cache. This
 * interface "zips" the complex primary keys into one "representative" key. Also this can be used to
 * override the default caching key and provide a custom key.
 */
public interface IMultiKeyCacheConverter<CacheKeyType> {

    /**
     * Converts the array of values into a singular representative key. The values are in order
     * of the primary key declaration and are the same length.
     *
     * @param values The values to convert into a singular key.
     * @return The non-null
     */
    @NonNull
    CacheKeyType getCachingKey(@NonNull Object[] values);
}
