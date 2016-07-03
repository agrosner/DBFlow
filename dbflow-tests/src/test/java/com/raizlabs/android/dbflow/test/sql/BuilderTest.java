package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Collate;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.property.PropertyFactory;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Author: andrewgrosner
 * Description: Test our {@link ConditionGroup} and
 * {@link Condition} classes to ensure they generate what they're supposed to.
 */
public class BuilderTest extends FlowTestCase {

    /**
     * This test will ensure that all column values are converted appropriately
     */
    @Test
    public void testConditions() {
        ConditionGroup conditionGroup = ConditionGroup.clause()
                .and(ConditionModel_Table.number.is(5L))
                .and(ConditionModel_Table.bytes.is(5))
                .and(ConditionModel_Table.fraction.is(6.5d));

        assertEquals("`number`=5 AND `bytes`=5 AND `fraction`=6.5", conditionGroup.getQuery());


        conditionGroup = ConditionGroup.clause()
                .and(ConditionModel_Table.number.between(5L).and(10L));
        assertEquals("`number` BETWEEN 5 AND 10", conditionGroup.getQuery().trim());
    }

    @Test
    public void testCollate() {
        Condition collate = ConditionModel_Table.name.is("James").collate(Collate.NOCASE);
        ConditionGroup conditionQueryBuilder = ConditionGroup.clause().and(collate);
        assertEquals("`name`='James' COLLATE NOCASE", conditionQueryBuilder.getQuery().trim());

    }

    @Test
    public void testChainingConditions() {
        ConditionGroup conditionQueryBuilder = ConditionGroup.clause();
        conditionQueryBuilder.and(ConditionModel_Table.name.is("James"))
                .or(ConditionModel_Table.number.is(6l))
                .and(ConditionModel_Table.fraction.is(4.5d));
        assertEquals("`name`='James' OR `number`=6 AND `fraction`=4.5", conditionQueryBuilder.getQuery().trim());
    }

    @Test
    public void testIsOperators() {
        ConditionGroup conditionQueryBuilder = ConditionGroup.clause()
                .and(ConditionModel_Table.name.is("James"))
                .or(ConditionModel_Table.fraction.isNotNull());
        assertEquals("`name`='James' OR `fraction` IS NOT NULL", conditionQueryBuilder.getQuery().trim());
    }

    @Test
    public void testInOperators() {
        Condition.In in = ConditionModel_Table.name.in("Jason", "Ryan", "Michael");
        ConditionGroup conditionQueryBuilder = ConditionGroup.clause().and(in);
        assertEquals("`name` IN ('Jason','Ryan','Michael')", conditionQueryBuilder.getQuery().trim());

        Condition.In notIn = ConditionModel_Table.name.notIn("Jason", "Ryan", "Michael");
        conditionQueryBuilder = ConditionGroup.clause().and(notIn);
        assertEquals("`name` NOT IN ('Jason','Ryan','Michael')", conditionQueryBuilder.getQuery().trim());
    }

    @Test
    public void testCombinedOperations() {
        ConditionGroup combinedCondition = ConditionGroup.clause()
                .and(ConditionGroup.clause()
                        .and(PropertyFactory.from(String.class, "A").eq(PropertyFactory.from(String.class, "B")))
                        .or(PropertyFactory.from(String.class, "B").eq(PropertyFactory.from(String.class, "C"))))
                .and(PropertyFactory.from(String.class, "C").eq("D"));
        assertEquals("(A=B OR B=C) AND C='D'", combinedCondition.getQuery());
    }

    @Test
    public void testCombinedOperationsReverse() {
        ConditionGroup combinedCondition = ConditionGroup.clause()
                .and(PropertyFactory.from(String.class, "C").eq("D"))
                .and(ConditionGroup.clause()
                        .and(PropertyFactory.from(String.class, "A")
                                .eq(PropertyFactory.from(String.class, "B")))
                        .or(PropertyFactory.from(String.class, "B")
                                .eq(PropertyFactory.from(String.class, "C"))));
        assertEquals("C='D' AND (A=B OR B=C)", combinedCondition.getQuery());

        String query = SQLite.select().from(TestModel1.class)
                .where(combinedCondition)
                .getQuery();
        assertEquals("SELECT * FROM `TestModel1` WHERE (C='D' AND (A=B OR B=C))", query.trim());
    }

}

