package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;
import static com.raizlabs.android.dbflow.sql.language.Condition.columnRaw;

/**
 * Description: Validates subquery formatting
 */
public class SubqueryTest extends FlowTestCase {


    public void testSubquery() {

        String query = new Select()
                .from(BoxedModel.class)
                .where().exists(new Select().from(BoxedModel.class)
                                        .where(columnRaw(BoxedModel$Table.INTEGERFIELD)
                                                       .eq(BoxedModel$Table.INTEGERFIELDNOTNULL)))
                                        .getQuery();

        assertEquals(
                "SELECT * FROM `BoxedModel` WHERE EXISTS (SELECT * FROM `BoxedModel` WHERE `integerField`=integerFieldNotNull)",
                query.trim());

        query = new Select()
                .from(BoxedModel.class)
                .where(column(BoxedModel$Table.INTEGERFIELD)
                                .greaterThan(new Select().avg(BoxedModel$Table.INTEGERFIELD).from(BoxedModel.class)
                                               .where(columnRaw(BoxedModel$Table.INTEGERFIELD)
                                                              .eq("BoxedModel." + BoxedModel$Table.INTEGERFIELD))))
                .getQuery();

        assertEquals(
                "SELECT * FROM `BoxedModel` WHERE `integerField`>" +
                    "(SELECT AVG(`integerField`)  FROM `BoxedModel` WHERE `integerField`=BoxedModel.integerField)",
                query.trim());
    }
}
