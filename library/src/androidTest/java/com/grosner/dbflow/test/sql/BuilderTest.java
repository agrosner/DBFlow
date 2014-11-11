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

    @Override
    protected String getDBName() {
        return "builder";
    }

    /**
     * This test will ensure that all column values are converted appropriately
     */
    public void testConditions() {
        ConditionQueryBuilder<ConditionModel> conditionQueryBuilder
                = new ConditionQueryBuilder<ConditionModel>(ConditionModel.class);
        byte[] bytes = new byte[] {5,5,5};
        conditionQueryBuilder.putConditions(
                Condition.column("number").is(5L),
                Condition.column("bytes").is(5),
                Condition.column("fraction").is(6.5d));

        assertEquals("number = 5 AND bytes = 5 AND fraction = 6.5", conditionQueryBuilder.getQuery());
    }

}
