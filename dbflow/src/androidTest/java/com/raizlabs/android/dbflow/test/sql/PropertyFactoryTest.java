package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.sql.language.property.ByteProperty;
import com.raizlabs.android.dbflow.sql.language.property.CharProperty;
import com.raizlabs.android.dbflow.sql.language.property.IntProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;
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
        assertEquals("'c' BETWEEN 'd' AND 'e'", queryBuilder.getQuery().trim());

        Property<String> stringProperty = PropertyFactory.from("MyGirl");
        assertEquals("'MyGirl'", stringProperty.getQuery());
        queryBuilder = new QueryBuilder();
        stringProperty.concatenate("Talkin' About").appendConditionToQuery(queryBuilder);
        assertEquals("'MyGirl'='MyGirl' || 'Talkin'' About'", queryBuilder.getQuery().trim());

        ByteProperty byteProperty = PropertyFactory.from((byte) 5);
        assertEquals("5", byteProperty.getQuery());
        queryBuilder = new QueryBuilder();
        byteProperty.in((byte) 6, (byte) 7, (byte) 8, (byte) 9).appendConditionToQuery(queryBuilder);
        assertEquals("5 IN (6,7,8,9)", queryBuilder.getQuery().trim());

        IntProperty intProperty = PropertyFactory.from(5);
        assertEquals("5", intProperty.getQuery());
        queryBuilder = new QueryBuilder();
        intProperty.greaterThan(TestModel2_Table.model_order).appendConditionToQuery(queryBuilder);
        assertEquals("5>`model_order`", queryBuilder.getQuery().trim());
    }


}
