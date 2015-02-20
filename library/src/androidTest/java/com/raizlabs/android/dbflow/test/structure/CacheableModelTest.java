package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.cache.BaseCacheableModel;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.test.FlowTestCase;

/**
 * Description:
 */
public class CacheableModelTest extends FlowTestCase {

    public void testCacheableModel() {

        Delete.table(CacheableModel.class);

        CacheableModel model = new CacheableModel();

        ModelCache<CacheableModel, ?> modelCache = BaseCacheableModel.getCache((Class<CacheableModel>) model.getClass());
        for (int i = 0; i < 100; i++) {
            model.name = "Test";
            model.id = 0;
            model.save(false);

            long id = model.id;
            CacheableModel cacheableModel = modelCache.get(id);
            assertNotNull(cacheableModel);

            assertEquals(Select.byId(CacheableModel.class, id), cacheableModel);

            model.delete(false);
            assertNull(modelCache.get(id));
        }

        Delete.table(CacheableModel.class);
    }
}
