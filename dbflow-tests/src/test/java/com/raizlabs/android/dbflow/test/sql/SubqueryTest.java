package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Test;

import static com.raizlabs.android.dbflow.test.sql.BoxedModel_Table.integerField;
import static org.junit.Assert.assertEquals;

/**
 * Description: Validates subquery formatting
 */
public class SubqueryTest extends FlowTestCase {


    @Test
    public void testSubquery() {

        String query = new Select()
                .from(BoxedModel.class)
                .where().exists(new Select().from(BoxedModel.class)
                        .where(integerField.eq(BoxedModel_Table.integerFieldNotNull)))
                .getQuery();

        assertEquals(
                "SELECT * FROM `BoxedModel` WHERE EXISTS (SELECT * FROM `BoxedModel` WHERE `integerField`=`integerFieldNotNull` )",
                query.trim());

        query = new Select()
                .from(BoxedModel.class)
                .where(integerField.greaterThan(new Select(Method.avg(integerField)).from(BoxedModel.class)
                        .where(integerField.eq(integerField.withTable()))))
                .getQuery();

        assertEquals(
                "SELECT * FROM `BoxedModel` WHERE `integerField`>" +
                        "(SELECT AVG(`integerField`) FROM `BoxedModel` WHERE `integerField`=`BoxedModel`.`integerField`)",
                query.trim());
    }
}
