package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.sql.language.property.ByteProperty;
import com.raizlabs.android.dbflow.sql.language.property.CharProperty;
import com.raizlabs.android.dbflow.sql.language.property.DoubleProperty;
import com.raizlabs.android.dbflow.sql.language.property.FloatProperty;
import com.raizlabs.android.dbflow.sql.language.property.IntProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;
import com.raizlabs.android.dbflow.sql.language.property.PropertyFactory;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table;
import com.raizlabs.android.dbflow.test.structure.TestModel2;
import com.raizlabs.android.dbflow.test.structure.TestModel2_Table;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Description:
 */
public class PropertyFactoryTest extends FlowTestCase {

    @Test
    public void test_delete() {
        long time = System.currentTimeMillis();

        Where<TestModel2> delete = SQLite.delete(TestModel2.class)
                .where(TestModel2_Table.model_order.plus(PropertyFactory.from(5)).lessThan((int) time));
        assertEquals("DELETE FROM `TestModel2` WHERE `model_order` + 5<" + (int) time, delete.getQuery().trim());
    }

    @Test
    public void test_charProperty() {
        CharProperty charProperty = PropertyFactory.from('c');
        assertEquals("'c'", charProperty.getQuery());
        QueryBuilder queryBuilder = new QueryBuilder();
        charProperty.between('d').and('e').appendConditionToQuery(queryBuilder);
        assertEquals("'c' BETWEEN 'd' AND 'e'", queryBuilder.getQuery().trim());
    }

    @Test
    public void test_stringProperty() {
        Property<String> stringProperty = PropertyFactory.from("MyGirl");
        assertEquals("'MyGirl'", stringProperty.getQuery());
        QueryBuilder queryBuilder = new QueryBuilder();
        stringProperty.concatenate("Talkin' About").appendConditionToQuery(queryBuilder);
        assertEquals("'MyGirl'='MyGirl' || 'Talkin'' About'", queryBuilder.getQuery().trim());
    }

    @Test
    public void test_byteProperty() {
        ByteProperty byteProperty = PropertyFactory.from((byte) 5);
        assertEquals("5", byteProperty.getQuery());
        QueryBuilder queryBuilder = new QueryBuilder();
        byteProperty.in((byte) 6, (byte) 7, (byte) 8, (byte) 9).appendConditionToQuery(queryBuilder);
        assertEquals("5 IN (6,7,8,9)", queryBuilder.getQuery().trim());
    }

    @Test
    public void test_intProperty() {
        IntProperty intProperty = PropertyFactory.from(5);
        assertEquals("5", intProperty.getQuery());
        QueryBuilder queryBuilder = new QueryBuilder();
        intProperty.greaterThan(TestModel2_Table.model_order).appendConditionToQuery(queryBuilder);
        assertEquals("5>`model_order`", queryBuilder.getQuery().trim());
    }

    @Test
    public void test_doubleProperty() {
        DoubleProperty doubleProperty = PropertyFactory.from(10d);
        assertEquals("10.0", doubleProperty.getQuery());
        QueryBuilder queryBuilder = new QueryBuilder();
        doubleProperty.plus(ConditionModel_Table.fraction).lessThan(ConditionModel_Table.fraction).appendConditionToQuery(queryBuilder);
        assertEquals("10.0 + `fraction`<`fraction`", queryBuilder.getQuery().trim());
    }

    @Test
    public void test_floatProperty() {
        FloatProperty floatProperty = PropertyFactory.from(20f);
        assertEquals("20.0", floatProperty.getQuery());
        QueryBuilder queryBuilder = new QueryBuilder();
        floatProperty.minus(ConditionModel_Table.floatie).minus(ConditionModel_Table.floatie).eq(5f).appendConditionToQuery(queryBuilder);
    }

    @Test
    public void test_queryProperty() {
        Property<TestModel1> model1Property = PropertyFactory.from(
                SQLite.select().from(TestModel1.class).where(TestModel1_Table.name.eq("Test"))).as("Cool");
        assertEquals("(SELECT * FROM `TestModel1` WHERE `name`='Test') AS `Cool`", model1Property.getDefinition());
        QueryBuilder queryBuilder = new QueryBuilder();
        model1Property.minus(ConditionModel_Table.fraction).plus(TestModel1_Table.name.withTable()).like("%somethingnotvalid%").appendConditionToQuery(queryBuilder);
        assertEquals("(SELECT * FROM `TestModel1` WHERE `name`='Test') - `fraction` + `TestModel1`.`name` LIKE '%somethingnotvalid%'", queryBuilder.getQuery().trim());

    }
}
