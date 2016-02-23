package com.raizlabs.android.dbflow.test.structure.caching;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CacheableModelTest extends FlowTestCase {

    @Test
    public void testCacheableModel() {

        Delete.table(CacheableModel.class);

        CacheableModel model = new CacheableModel();

        ModelCache<CacheableModel, ?> modelCache = FlowManager.getModelAdapter(CacheableModel.class).getModelCache();
        for (int i = 0; i < 100; i++) {
            model.name = "Test";
            model.save();
            assertTrue(model.exists());

            Long id = model.id;
            CacheableModel cacheableModel = modelCache.get(id);
            assertNotNull(cacheableModel);

            assertEquals(SQLite.select().from(CacheableModel.class).
                    where(CacheableModel_Table.id.is(id))
                    .querySingle(), cacheableModel);

            model.delete();
            assertNull(modelCache.get(id));
        }

        Delete.table(CacheableModel.class);
    }

    @Test
    public void testCacheableModel2() {
        Delete.table(CacheableModel2.class);

        CacheableModel2 model = new CacheableModel2();

        ModelCache<CacheableModel2, ?> modelCache = FlowManager.getModelAdapter(CacheableModel2.class).getModelCache();
        for (int i = 0; i < 100; i++) {
            model.id = i;
            model.save();

            Integer id = model.id;
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

    @Test
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

    @Test
    public void testCacheableModel4() {
        List<CacheableModel4> model4s = SQLite.select()
                .from(CacheableModel4.class)
                .where(CacheableModel4_Table.id.eq(4))
                .queryList();
    }

    @Test
    public void testMultiplePrimaryKey() {
        Delete.table(MultipleCacheableModel.class);

        MultipleCacheableModel cacheableModel = new MultipleCacheableModel();
        ModelAdapter<MultipleCacheableModel> modelAdapter = FlowManager.getModelAdapter(MultipleCacheableModel.class);
        ModelCache<MultipleCacheableModel, ?> modelCache = modelAdapter.getModelCache();
        Object[] values = new Object[modelAdapter.getCachingColumns().length];
        for (int i = 0; i < 25; i++) {
            cacheableModel.latitude = i;
            cacheableModel.longitude = 25;
            cacheableModel.save();

            MultipleCacheableModel model = modelCache.get(MultipleCacheableModel.multiKeyCacheModel
                    .getCachingKey(modelAdapter.getCachingColumnValuesFromModel(values, cacheableModel)));
            assertNotNull(model);
            assertEquals(cacheableModel, model);
            assertEquals(SQLite.select().from(MultipleCacheableModel.class)
                    .where(MultipleCacheableModel_Table.latitude.eq(cacheableModel.latitude))
                    .and(MultipleCacheableModel_Table.longitude.eq(cacheableModel.longitude)).querySingle(), model);

            model.delete();
            assertNull(modelCache.get(MultipleCacheableModel.multiKeyCacheModel
                    .getCachingKey(modelAdapter.getCachingColumnValuesFromModel(values, model))));
        }

        Delete.table(MultipleCacheableModel.class);
    }
}
