package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Collate;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;
import static com.raizlabs.android.dbflow.sql.language.NameAlias.columnRaw;

/**
 * Author: andrewgrosner
 * Description: Test our {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder} and
 * {@link Condition} classes to ensure they generate what they're supposed to.
 */
public class BuilderTest extends FlowTestCase {

    /**
     * This test will ensure that all column values are converted appropriately
     */
    public void testConditions() {
        ConditionQueryBuilder<ConditionModel> conditionQueryBuilder
                = new ConditionQueryBuilder<>(ConditionModel.class);
        conditionQueryBuilder.addConditions(
                column("number").is(5L),
                column("bytes").is(5),
                column("fraction").is(6.5d));

        assertEquals("`number`=5 AND `bytes`=5 AND `fraction`=6.5", conditionQueryBuilder.getQuery());


        conditionQueryBuilder = new ConditionQueryBuilder<>(ConditionModel.class)
                .addCondition(column(ConditionModel$Table.NUMBER).between(5L).and(10L));
        assertEquals("`number` BETWEEN 5 AND 10", conditionQueryBuilder.getQuery().trim());
    }

    public void testCollate() {
        Condition collate = column(ConditionModel$Table.NAME).is("James").collate(Collate.NOCASE);
        ConditionQueryBuilder<ConditionModel> conditionQueryBuilder = new ConditionQueryBuilder<>(ConditionModel.class);
        collate.appendConditionToQuery(conditionQueryBuilder);

        assertEquals("`name`='James' COLLATE NOCASE", conditionQueryBuilder.getQuery().trim());

    }

    public void testChainingConditions() {
        ConditionQueryBuilder<ConditionModel> conditionQueryBuilder = new ConditionQueryBuilder<>(ConditionModel.class);
        conditionQueryBuilder.addCondition(column(ConditionModel$Table.NAME).is("James").separator("OR"))
                .addCondition(column(ConditionModel$Table.NUMBER).is(6).separator("AND"))
                .addCondition(column(ConditionModel$Table.FRACTION).is(4.5d));
        assertEquals("`name`='James' OR `number`=6 AND `fraction`=4.5", conditionQueryBuilder.getQuery().trim());
    }

    public void testIsOperators() {
        ConditionQueryBuilder<ConditionModel> conditionQueryBuilder = new ConditionQueryBuilder<>(ConditionModel.class);
        conditionQueryBuilder.addCondition(column(ConditionModel$Table.NAME).is("James"))
                .or(column(ConditionModel$Table.FRACTION).isNotNull());
        assertEquals("`name`='James' OR `fraction` IS NOT NULL", conditionQueryBuilder.getQuery().trim());
    }

    public void testInOperators() {
        Condition.In in = column(ConditionModel$Table.NAME).in("Jason", "Ryan", "Michael");
        ConditionQueryBuilder<ConditionModel> conditionQueryBuilder = new ConditionQueryBuilder<>(ConditionModel.class, in);
        assertEquals("`name` IN ('Jason','Ryan','Michael')", conditionQueryBuilder.getQuery().trim());

        Condition.In notIn = column(ConditionModel$Table.NAME).notIn("Jason", "Ryan", "Michael");
        conditionQueryBuilder = new ConditionQueryBuilder<>(ConditionModel.class, notIn);
        assertEquals("`name` NOT IN ('Jason','Ryan','Michael')", conditionQueryBuilder.getQuery().trim());
    }

    public void testCombinedOperations() {
        Condition.CombinedCondition combinedCondition = Condition.CombinedCondition
                .begin(Condition.CombinedCondition
                        .begin(column(columnRaw("A"))).or(column(columnRaw("B"))))
                .and(column(columnRaw("C")));
        ConditionQueryBuilder<ConditionModel> conditionQueryBuilder = new ConditionQueryBuilder<>(ConditionModel.class, combinedCondition);
        assertEquals("((A OR B) AND C)", conditionQueryBuilder.getQuery());
    }

}
