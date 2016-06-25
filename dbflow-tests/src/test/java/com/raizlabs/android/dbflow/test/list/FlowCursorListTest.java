package com.raizlabs.android.dbflow.test.list;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.cache.ModelLruCache;
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.querymodel.TestQueryModel;
import com.raizlabs.android.dbflow.test.structure.TestModel1;
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class FlowCursorListTest extends FlowTestCase {


    @Test
    public void test_validateBuilderCacheable() {
        FlowCursorList<TestModel1> cursorList =
                new FlowCursorList.Builder<>(TestModel1.class)
                        .cacheSize(50)
                        .modelCache(ModelLruCache.<TestModel1>newInstance(50))
                        .modelQueriable(SQLite.select().from(TestModel1.class))
                        .build();

        assertEquals(TestModel1.class, cursorList.getTable());
        assertEquals(50, cursorList.cacheSize());
        assertTrue(cursorList.modelCache() instanceof ModelLruCache);
        assertEquals(true, cursorList.cachingEnabled());
        assertNotNull(cursorList.cursor());

        cursorList.close();
    }

    @Test
    public void test_validateBuilderCustomQuery() {
        FlowCursorList<TestQueryModel> cursorList
                = new FlowCursorList.Builder<>(TestQueryModel.class)
                .cursor(SQLite
                        .select(TestModel1_Table.name.as("newName"))
                        .from(TestModel1.class).query())
                .build();

        assertEquals(TestQueryModel.class, cursorList.table());
        assertTrue(cursorList.cachingEnabled());
        assertEquals(0, cursorList.cacheSize());

        cursorList.close();
    }

    @Test
    public void test_ensureModelCache() {
        Delete.table(TestModel1.class);

        TestModel1 model = new TestModel1();
        model.setName("Test");
        model.save();

        FlowCursorList<TestModel1> list = new FlowCursorList.Builder<>(TestModel1.class)
                .cacheModels(true)
                .modelQueriable(SQLite.select().from(TestModel1.class))
                .build();
        assertEquals(1, list.getCount());
        assertEquals(model.getName(), list.getItem(0).getName());
        assertEquals(list.getItem(0), list.getItem(0));

        list.close();
    }

    @Test
    public void test_canIterateCursor() {
        List<TestModel1> models = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            TestModel1 model = new TestModel1();
            model.setName("" + i);
            models.add(model);
        }
        FlowManager.getDatabase(TestDatabase.class)
                .executeTransaction(FastStoreModelTransaction
                        .insertBuilder(FlowManager.getModelAdapter(TestModel1.class))
                        .addAll(models)
                        .build());

        FlowCursorList<TestModel1> cursorList = SQLite.select()
                .from(TestModel1.class).cursorList();
        assertNotEquals(0, cursorList.getCount());
        assertEquals(50, cursorList.getCount());
        int count = 0;
        for (TestModel1 model : cursorList) {
            assertEquals(count + "", model.getName());
            count++;
        }

    }
}
