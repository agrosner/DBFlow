package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.sql.language.property.CharProperty;
import com.raizlabs.android.dbflow.sql.language.property.PropertyFactory;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel2;
import com.raizlabs.android.dbflow.test.structure.TestModel2_Table;

/**
 * Description:
 */
public class PropertyFactoryTest extends FlowTestCase {

    public void testPropertyFactory() {

        long time = System.currentTimeMillis();

        Where<TestModel2> delete = SQLite.delete(TestModel2.class)
                .where(TestModel2_Table.model_order.plus(PropertyFactory.from(5)).lessThan((int) time));
        assertEquals("DELETE FROM `TestModel2` WHERE `model_order` + 5<" + (int) time, delete.getQuery().trim());

        CharProperty charProperty = PropertyFactory.from('c');
        assertEquals("'c'", charProperty.getQuery());
        QueryBuilder queryBuilder = new QueryBuilder();
        charProperty.between('d').and('e').appendConditionToQuery(queryBuilder);
        assertEquals("'c' BETWEEN 'd' AND 'b'", queryBuilder.getQuery());
    }


}
