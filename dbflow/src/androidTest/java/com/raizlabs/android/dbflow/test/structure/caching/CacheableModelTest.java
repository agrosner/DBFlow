package com.raizlabs.android.dbflow.test.structure.caching;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import java.util.List;

public class CacheableModelTest extends FlowTestCase {

    public void testCacheableModel() {

        Delete.table(CacheableModel.class);

        CacheableModel model = new CacheableModel();

        ModelCache<CacheableModel, ?> modelCache = FlowManager.getModelAdapter(CacheableModel.class).getModelCache();
        for (int i = 0; i < 100; i++) {
            model.name = "Test";
            model.save();
            assertTrue(model.exists());

            long id = model.id;
            CacheableModel cacheableModel = modelCache.get(id);
            assertNotNull(cacheableModel);

            assertEquals(new Select().from(CacheableModel.class).
                    where(CacheableModel_Table.id.is(id))
                    .querySingle(), cacheableModel);

            model.delete();
            assertNull(modelCache.get(id));
        }

        Delete.table(CacheableModel.class);
    }

    public void testCacheableModel2() {
        Delete.table(CacheableModel2.class);

        CacheableModel2 model = new CacheableModel2();

        ModelCache<CacheableModel2, ?> modelCache = FlowManager.getModelAdapter(CacheableModel2.class).getModelCache();
        for (int i = 0; i < 100; i++) {
            model.id = i;
            model.save();

            long id = model.id;
            CacheableModel2 cacheableModel = modelCache.get(id);
            assertNotNull(cacheableModel);

            assertEquals(new Select().from(CacheableModel2.class)
                    .where(CacheableModel2_Table.id.is((int) id))
                    .querySingle(), cacheableModel);

            model.delete();
            assertNull(modelCache.get(id));
        }

        Delete.table(CacheableModel2.class);
    }

    public void testCacheableModel3() {
        Delete.table(CacheableModel3.class);

        CacheableModel3 cacheableModel3 = new CacheableModel3();

        ModelCache<CacheableModel3, ?> modelCache = FlowManager.getModelAdapter(CacheableModel3.class).getModelCache();
        for (int i = 0; i < 20; i++) {
            cacheableModel3.number = i;
            cacheableModel3.cache_id = "model" + i;
            cacheableModel3.save();

            String id = cacheableModel3.cache_id;
            CacheableModel3 cacheableModel = modelCache.get(id);
            assertNotNull(cacheableModel);

            assertEquals(new Select().from(CacheableModel3.class)
                    .where(CacheableModel3_Table.cache_id.is(id))
                    .querySingle(), cacheableModel);

            cacheableModel3.delete();
            assertNull(modelCache.get(id));
        }

        Delete.table(CacheableModel3.class);

    }

    public void testCacheableModel4() {
        List<CacheableModel4> model4s = SQLite.select()
                .from(CacheableModel4.class)
                .where(CacheableModel4_Table.id.eq(4))
                .queryList();
    }

    public void testMultiplePrimaryKey() {
        Delete.table(MultipleCacheableModel.class);

        MultipleCacheableModel cacheableModel = new MultipleCacheableModel();
        ModelCache<MultipleCacheableModel, ?> modelCache = FlowManager.getModelAdapter(MultipleCacheableModel.class).getModelCache();
        for (int i = 0; i < 25; i++) {
            cacheableModel.latitude = i;
            cacheableModel.longitude = 25;
            cacheableModel.save();

            MultipleCacheableModel model = modelCache.get(MultipleCacheableModel.multiKeyCacheModel.getCachingKey(cacheableModel));
            assertNotNull(model);
            assertEquals(cacheableModel, model);
            assertEquals(SQLite.select().from(MultipleCacheableModel.class)
                    .where(MultipleCacheableModel_Table.latitude.eq(cacheableModel.latitude))
                    .and(MultipleCacheableModel_Table.longitude.eq(cacheableModel.longitude)).querySingle(), model);

            model.delete();
            assertNull(modelCache.get(MultipleCacheableModel.multiKeyCacheModel.getCachingKey(model)));
        }

        Delete.table(MultipleCacheableModel.class);
    }
}
