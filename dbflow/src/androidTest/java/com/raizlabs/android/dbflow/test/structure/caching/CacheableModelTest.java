package com.raizlabs.android.dbflow.test.structure.caching;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.cache.BaseCacheableModel;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

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
            model.save();
            assertTrue(model.exists());

            long id = model.id;
            CacheableModel cacheableModel = modelCache.get(id);
            assertNotNull(cacheableModel);

            assertEquals(new Select().from(CacheableModel.class).
                    where(column(CacheableModel$Table.ID).is(id))
                    .querySingle(), cacheableModel);

            model.delete();
            assertNull(modelCache.get(id));
        }

        Delete.table(CacheableModel.class);
    }

    public void testCacheableModel2() {
        Delete.table(CacheableModel2.class);

        CacheableModel2 model = new CacheableModel2();

        ModelCache<CacheableModel2, ?> modelCache = BaseCacheableModel.getCache((Class<CacheableModel2>) model.getClass());
        for (int i = 0; i < 100; i++) {
            model.id = i;
            model.save();

            long id = model.id;
            CacheableModel2 cacheableModel = modelCache.get(id);
            assertNotNull(cacheableModel);

            assertEquals(new Select().from(CacheableModel2.class)
                    .where(column(CacheableModel2$Table.ID).is(id))
                    .querySingle(), cacheableModel);

            model.delete();
            assertNull(modelCache.get(id));
        }

        Delete.table(CacheableModel2.class);
    }

    public void testCacheableModel3() {
        Delete.table(CacheableModel3.class);

        CacheableModel3 cacheableModel3 = new CacheableModel3();

        ModelCache<CacheableModel3, ?> modelCache = BaseCacheableModel.getCache(CacheableModel3.class);
        for(int i = 0; i < 20; i++) {
            cacheableModel3.number = i;
            cacheableModel3.cache_id = "model" + i;
            cacheableModel3.save();

            String id = cacheableModel3.cache_id;
            CacheableModel3 cacheableModel = modelCache.get(id);
            assertNotNull(cacheableModel);

            assertEquals(new Select().from(CacheableModel3.class)
                                 .where(column(CacheableModel3$Table.CACHE_ID).is(id))
                                 .querySingle(), cacheableModel);

            cacheableModel3.delete();
            assertNull(modelCache.get(id));
        }

        Delete.table(CacheableModel3.class);

    }
}
