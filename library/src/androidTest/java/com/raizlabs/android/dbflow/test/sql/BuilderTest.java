package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Collate;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.test.FlowTestCase;

/**
 * Author: andrewgrosner
 * Description: Test our {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder} and
 * {@link com.raizlabs.android.dbflow.sql.builder.Condition} classes to ensure they generate what they're supposed to.
 */
public class BuilderTest extends FlowTestCase {

    /**
     * This test will ensure that all column values are converted appropriately
     */
    public void testConditions() {
        ConditionQueryBuilder<ConditionModel> conditionQueryBuilder
                = new ConditionQueryBuilder<ConditionModel>(ConditionModel.class);
        conditionQueryBuilder.putConditions(
                Condition.column("number").is(5L),
                Condition.column("bytes").is(5),
                Condition.column("fraction").is(6.5d));

        assertEquals("number=5 AND bytes=5 AND fraction=6.5", conditionQueryBuilder.getQuery());


        conditionQueryBuilder = new ConditionQueryBuilder<>(ConditionModel.class)
                .putCondition(Condition.column(ConditionModel$Table.NUMBER).between(5L).and(10L));
        assertEquals("number BETWEEN 5 AND 10", conditionQueryBuilder.getQuery().trim());
    }

    public void testCollate() {
        Condition collate = Condition.column(ConditionModel$Table.NAME).is("James").collate(Collate.NOCASE);
        ConditionQueryBuilder<ConditionModel> conditionQueryBuilder = new ConditionQueryBuilder<>(ConditionModel.class);
        collate.appendConditionToQuery(conditionQueryBuilder);

        assertEquals("name='James' COLLATE NOCASE", conditionQueryBuilder.getQuery().trim());

    }

    public void testChainingConditions() {
        ConditionQueryBuilder<ConditionModel> conditionQueryBuilder = new ConditionQueryBuilder<>(ConditionModel.class);
        conditionQueryBuilder.putCondition(Condition.column(ConditionModel$Table.NAME).is("James").separator("OR"))
                .putCondition(Condition.column(ConditionModel$Table.NUMBER).is(6).separator("AND"))
                .putCondition(Condition.column(ConditionModel$Table.FRACTION).is(4.5d));
        assertEquals("name='James' OR number=6 AND fraction=4.5", conditionQueryBuilder.getQuery().trim());
    }

    public void testIsOperators() {
        ConditionQueryBuilder<ConditionModel> conditionQueryBuilder = new ConditionQueryBuilder<>(ConditionModel.class);
        conditionQueryBuilder.putCondition(Condition.column(ConditionModel$Table.NAME).is("James"))
                .or(Condition.column(ConditionModel$Table.FRACTION).isNotNull());
        assertEquals("name='James' OR fraction IS NOT NULL", conditionQueryBuilder.getQuery().trim());
    }

    public void testInOperators() {
        Condition.In in = Condition.column(ConditionModel$Table.NAME).in("Jason").and("Ryan").and("Michael");
        ConditionQueryBuilder<ConditionModel> conditionQueryBuilder = new ConditionQueryBuilder<>(ConditionModel.class, in);
        assertEquals(conditionQueryBuilder.getQuery().trim(), "name IN ('Jason','Ryan','Michael')");

        Condition.In notIn = Condition.column(ConditionModel$Table.NAME).notIn("Jason").and("Ryan").and("Michael");
        conditionQueryBuilder = new ConditionQueryBuilder<>(ConditionModel.class, notIn);
        assertEquals(conditionQueryBuilder.getQuery().trim(), "name NOT IN ('Jason','Ryan','Michael')");
    }

}
