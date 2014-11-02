package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.test.FlowTestCase;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class DBStructureTest extends FlowTestCase {

    @Override
    protected String getDBName() {
        return "dbstructure";
    }

    // region Test Primary Where Query

    public void testPrimaryWhereQuery() {
        ConditionQueryBuilder<TestPrimaryWhere> primaryWhere = FlowManager.getPrimaryWhereQuery(TestPrimaryWhere.class);
        assertEquals(primaryWhere.getQuery(), "name = ? AND location = ?");
    }

    // endregion Test Primary Where Query


}
