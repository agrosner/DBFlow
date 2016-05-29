package com.raizlabs.android.dbflow.test.sql.fast;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.contentobserver.ContentObserverModel;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Description:
 */
public class FastModelTest extends FlowTestCase {

    @Test
    public void test_canLoadQuery() {
        Delete.table(FastModel.class);

        List<FastModel> testModel1s = getRandomFastModels(100, FastModel.class);

        FlowManager.getDatabase(TestDatabase.class)
                .executeTransaction(FastStoreModelTransaction
                        .insertBuilder(FlowManager.getModelAdapter(FastModel.class))
                        .addAll(testModel1s).build());

        testModel1s = SQLite.select()
                .from(FastModel.class).queryList();
        for (int i = 0; i < testModel1s.size(); i++) {
            FastModel fastModel = testModel1s.get(i);
            assertEquals(i, fastModel.id);
            assertNotNull(fastModel.contentObserverModel);
        }

        Delete.table(FastModel.class);
    }

    @Test
    public void test_speed() {

        // dont pollute results with initialization
        SQLite.select().from(NonFastModel.class).queryList();
        SQLite.select().from(FastModel.class).queryList();

        List<NonFastModel> nonFastList = getRandomFastModels(5000, NonFastModel.class);
        FlowManager.getDatabase(TestDatabase.class)
                .executeTransaction(FastStoreModelTransaction
                        .insertBuilder(FlowManager.getModelAdapter(NonFastModel.class))
                        .addAll(nonFastList).build());

        long startTime = System.currentTimeMillis();

        nonFastList = SQLite.select().from(NonFastModel.class).queryList();

        System.out.println("Loading for less fast took: " + (System.currentTimeMillis() - startTime));

        Delete.tables(NonFastModel.class, ContentObserverModel.class);

        List<FastModel> list = getRandomFastModels(5000, FastModel.class);
        FlowManager.getDatabase(TestDatabase.class)
                .executeTransaction(FastStoreModelTransaction
                        .insertBuilder(FlowManager.getModelAdapter(FastModel.class))
                        .addAll(list).build());

        startTime = System.currentTimeMillis();
        list = SQLite.select().from(FastModel.class).queryList();

        System.out.println("Loading for fast took: " + (System.currentTimeMillis() - startTime));

    }

    @NonNull
    private <T extends FastModel> List<T> getRandomFastModels(int count, Class<T> fastModelClass) {
        List<T> testModel1s = new ArrayList<>();
        T testModel1;
        for (int i = 0; i < count; i++) {
            testModel1 = FlowManager.getModelAdapter(fastModelClass).newInstance();
            testModel1.name = UUID.randomUUID().toString();
            testModel1.id = i;
            testModel1.date = new Date(System.currentTimeMillis());
            ContentObserverModel contentObserverModel = new ContentObserverModel();
            contentObserverModel.setName(UUID.randomUUID().toString());
            contentObserverModel.setId(i);
            contentObserverModel.insert();
            testModel1.contentObserverModel = contentObserverModel;
            testModel1s.add(testModel1);
        }
        return testModel1s;
    }
}
