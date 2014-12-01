package com.grosner.dbflow.test.sql;

import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.test.FlowTestCase;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
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

        assertEquals("number = 5 AND bytes = 5 AND fraction = 6.5", conditionQueryBuilder.getQuery());
    }

}
