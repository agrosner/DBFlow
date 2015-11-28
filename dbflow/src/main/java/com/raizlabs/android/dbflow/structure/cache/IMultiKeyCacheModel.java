package com.raizlabs.android.dbflow.structure.cache;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: This interface allows for {@link Model} to have multiple primary keys in a cache. This
 * interface "zips" the complex primary keys into one "representative" key. Also this can be used to
 * override the default caching key and provide a custom key.
 */
public interface IMultiKeyCacheModel<CacheKeyType> {

    CacheKeyType getCachingKey();
}
