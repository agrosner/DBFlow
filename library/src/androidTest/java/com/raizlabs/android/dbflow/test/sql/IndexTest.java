package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.index.Index;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import java.util.List;

/**
 * Description:
 */
public class IndexTest extends FlowTestCase {

    public void testIndex() {

        Delete.table(IndexModel.class);

        Index<IndexModel> modelIndex = new Index<IndexModel>("salary_index")
                .on(IndexModel.class, IndexModel$Table.SALARY);
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

        List<IndexModel> list = new Select().from(IndexModel.class)
                .indexedBy(modelIndex.getIndexName())
                .where(Condition.column(IndexModel$Table.SALARY).greaterThan(20000)).queryList();

        assertTrue(list.size() == 1);

        modelIndex.disable();

        Delete.table(IndexModel.class);

    }
}
