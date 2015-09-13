/*
package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Index;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import java.util.List;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

public class IndexTest extends FlowTestCase {

    public void testIndex() {

        Delete.table(IndexModel.class);

        Index<IndexModel> modelIndex = new Index<IndexModel>("salary_index")
                .on(IndexModel.class, IndexModel_Table.salary);
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

        // TODO: generate index property.
        List<IndexModel> list = new Select().from(IndexModel.class)
                .indexedBy(modelIndex.getIndexName())
                .where(column(IndexModel_Table.salary).greaterThan(20000)).queryList();

        assertTrue(list.size() == 1);

        modelIndex.disable();

        Delete.table(IndexModel.class);

    }
}
*/

