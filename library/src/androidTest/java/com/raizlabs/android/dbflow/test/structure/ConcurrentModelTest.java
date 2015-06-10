package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Insert;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.TestDatabase;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrentModelTest extends FlowTestCase {
    private static final long CONCURRENT_INSERT_COUNT = 10;
    private static final long CONCURRENT_INSERT_TIMEOUT = 60 * 1000;

    public void testConcurrentInsert() throws InterruptedException {
        Delete.table(TestModel1.class);

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        for (int i = 0; i < CONCURRENT_INSERT_COUNT; i++) {
            executorService.execute(new InsertRunnable());            // fails
            //executorService.execute(new PlainSqlInsertRunnable());    // passes
        }

        executorService.shutdown();
        executorService.awaitTermination(CONCURRENT_INSERT_TIMEOUT, TimeUnit.MILLISECONDS);

        long modelCount = new Select().count().from(TestModel1.class).count();
        assertEquals(CONCURRENT_INSERT_COUNT, modelCount);
    }

    private static class InsertRunnable implements Runnable {
        private InsertRunnable() {
        }

        @Override
        public void run() {
            String uuid = UUID.randomUUID().toString();

            TestModel1 indexModel = new TestModel1();
            indexModel.name = uuid;

            indexModel.insert();
        }
    }

    private static class PlainSqlInsertRunnable implements Runnable {
        private PlainSqlInsertRunnable() {
        }

        @Override
        public void run() {
            String uuid = UUID.randomUUID().toString();

            Insert insert = Insert.into(TestModel1.class).orFail()
                    .values(uuid);

            FlowManager.getDatabase(TestDatabase.NAME).getWritableDatabase().execSQL(insert.getQuery());
        }
    }
}
