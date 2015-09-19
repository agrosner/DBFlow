package com.raizlabs.android.dbflow.test.sql.index;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Index;
import com.raizlabs.android.dbflow.sql.language.property.IndexProperty;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import java.util.List;

import static com.raizlabs.android.dbflow.test.sql.index.IndexModel_Table.salary;

public class IndexTest extends FlowTestCase {

    public void testIndex() {

        Delete.table(IndexModel.class);

        Index<IndexModel> modelIndex = new Index<IndexModel>("salary_index")
                .on(IndexModel.class, salary);
        modelIndex.disable();

        assertEquals("CREATE INDEX IF NOT EXISTS `salary_index` ON `IndexModel`(`salary`)", modelIndex.getQuery().trim());

        modelIndex.enable();

        IndexModel indexModel = new IndexModel();
        indexModel.name = "Index";
        indexModel.salary = 30000;
        indexModel.save();

        indexModel = new IndexModel();
        indexModel.name = "Index2";
        indexModel.salary = 15000;
        indexModel.save();

        IndexProperty<IndexModel> indexProperty = new IndexProperty<>(modelIndex.getIndexName(),
                true, IndexModel.class, salary);

        List<IndexModel> list = new Select().from(IndexModel.class)
                .indexedBy(indexProperty)
                .where(salary.greaterThan(20000l)).queryList();

        assertTrue(list.size() == 1);

        modelIndex.disable();

        Delete.table(IndexModel.class);

    }
}

