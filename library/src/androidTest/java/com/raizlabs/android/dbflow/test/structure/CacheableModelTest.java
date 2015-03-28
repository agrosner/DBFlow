package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.sql.builder.Condition;
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
            model.save();

            long id = model.id;
            CacheableModel cacheableModel = modelCache.get(id);
            assertNotNull(cacheableModel);

            assertEquals(new Select().from(CacheableModel.class).
                    where(Condition.column(CacheableModel$Table.NAME).is(id))
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
                    .where(Condition.column(CacheableModel$Table.NAME).is(id))
                    .querySingle(), cacheableModel);

            model.delete();
            assertNull(modelCache.get(id));
        }

        Delete.table(CacheableModel2.class);
    }
}
