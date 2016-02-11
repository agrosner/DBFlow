package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import java.util.List;

/**
 * Description: Tests the {@link OneToMany} annotation to ensure it works as expected.
 */
public class OneToManyModelTest extends FlowTestCase {

    public void testOneToManyModel() {
        Delete.tables(TestModel2.class, OneToManyModel.class);

        TestModel2 testModel2 = new TestModel2();
        testModel2.name = "Greater";
        testModel2.order = 4;
        testModel2.save();

        testModel2 = new TestModel2();
        testModel2.name = "Lesser";
        testModel2.order = 1;
        testModel2.save();

        // assert we save
        OneToManyModel oneToManyModel = new OneToManyModel();
        oneToManyModel.name = "HasOrders";
        oneToManyModel.save();
        assertTrue(oneToManyModel.exists());

        // assert loading works as expected.
        oneToManyModel = new Select().from(OneToManyModel.class).querySingle();
        assertNotNull(oneToManyModel.orders);
        assertTrue(!oneToManyModel.orders.isEmpty());

        // assert the deletion cleared the variable
        oneToManyModel.delete();
        assertFalse(oneToManyModel.exists());
        assertNull(oneToManyModel.orders);

        // assert singular relationship was deleted.
        List<TestModel2> list = new Select().from(TestModel2.class).queryList();
        assertTrue(list.size() == 1);

        Delete.tables(TestModel2.class, OneToManyModel.class);
    }

}
