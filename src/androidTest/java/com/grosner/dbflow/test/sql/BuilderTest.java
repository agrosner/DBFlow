package com.grosner.dbflow.test.sql;

import android.test.AndroidTestCase;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.structure.Column;
import com.grosner.dbflow.test.FlowTestCase;
import com.grosner.dbflow.test.structure.TestModel1;

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

    @Override
    protected void modifyConfiguration(DBConfiguration.Builder builder) {
        builder.addModelClasses(ConditionModel.class);
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
                Condition.column("bytes").is(bytes),
                Condition.column("fraction").is(6.5d));

        assertEquals("number = 5 AND bytes = '" + bytes.toString() + "' AND fraction = 6.5", conditionQueryBuilder.getQuery());
    }

    private static class ConditionModel extends TestModel1 {
        @Column
        long number;

        @Column
        byte[] bytes;

        @Column
        double fraction;
    }
}
