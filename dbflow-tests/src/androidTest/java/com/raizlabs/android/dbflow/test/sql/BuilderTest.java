package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Collate;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.property.PropertyFactory;
import com.raizlabs.android.dbflow.test.FlowTestCase;

/**
 * Author: andrewgrosner
 * Description: Test our {@link ConditionGroup} and
 * {@link Condition} classes to ensure they generate what they're supposed to.
 */
public class BuilderTest extends FlowTestCase {

    /**
     * This test will ensure that all column values are converted appropriately
     */
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

    public void testCollate() {
        Condition collate = ConditionModel_Table.name.is("James").collate(Collate.NOCASE);
        ConditionGroup conditionQueryBuilder = ConditionGroup.clause().and(collate);
        assertEquals("`name`='James' COLLATE NOCASE", conditionQueryBuilder.getQuery().trim());

    }

    public void testChainingConditions() {
        ConditionGroup conditionQueryBuilder = ConditionGroup.clause();
        conditionQueryBuilder.and(ConditionModel_Table.name.is("James"))
                .or(ConditionModel_Table.number.is(6l))
                .and(ConditionModel_Table.fraction.is(4.5d));
        assertEquals("`name`='James' OR `number`=6 AND `fraction`=4.5", conditionQueryBuilder.getQuery().trim());
    }

    public void testIsOperators() {
        // TODO: add is not null/null ops
        //ConditionGroup conditionQueryBuilder = ConditionGroup.clause()
        //        .and(ConditionModel_Table.name.is("James"))
        //        .or(ConditionModel_Table.fraction.isNotNull());
        //assertEquals("`name`='James' OR `fraction` IS NOT NULL", conditionQueryBuilder.getQuery().trim());
    }

    public void testInOperators() {
        Condition.In in = ConditionModel_Table.name.in("Jason", "Ryan", "Michael");
        ConditionGroup conditionQueryBuilder = ConditionGroup.clause().and(in);
        assertEquals("`name` IN ('Jason','Ryan','Michael')", conditionQueryBuilder.getQuery().trim());

        Condition.In notIn = ConditionModel_Table.name.notIn("Jason", "Ryan", "Michael");
        conditionQueryBuilder = ConditionGroup.clause().and(notIn);
        assertEquals("`name` NOT IN ('Jason','Ryan','Michael')", conditionQueryBuilder.getQuery().trim());
    }

    public void testCombinedOperations() {
        // TODO: fix combined ops
        ConditionGroup combinedCondition = ConditionGroup.clause()
                .and(ConditionGroup.clause()
                        .and(PropertyFactory.from(String.class, "A").eq(PropertyFactory.from(String.class, "B")))
                        .or(PropertyFactory.from(String.class, "B").eq(PropertyFactory.from(String.class, "C"))))
                .and(PropertyFactory.from(String.class, "C").eq("D"));
        assertEquals("(A=B OR B=C) AND C='D'", combinedCondition.getQuery());
    }

}

