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

        ModelCache<CacheableModel, ?> modelCache = null;
        for (int i = 0; i < 1000; i++) {
            CacheableModel model = new CacheableModel();
            model.name = "Test";
            model.save(false);

            if (modelCache == null) {
                modelCache = BaseCacheableModel.getCache((Class<CacheableModel>) model.getClass());
            }

            long id = model.id;
            assertNotNull(modelCache.get(id));

            assertEquals(Select.byId(CacheableModel.class, id), modelCache.get(id));

            model.delete(false);
            assertNull(modelCache.get(id));
        }

        Delete.table(CacheableModel.class);
    }
}
