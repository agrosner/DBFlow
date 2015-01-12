package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.test.FlowTestCase;

/**
 * Description:
 */
public class DBStructureTest extends FlowTestCase {

    // region Test Primary Where Query

    public void testPrimaryWhereQuery() {
        ConditionQueryBuilder<TestPrimaryWhere> primaryWhere = FlowManager.getPrimaryWhereQuery(TestPrimaryWhere.class);
        assertEquals(primaryWhere.getQuery(), "name=? AND location=?");
    }

    // endregion Test Primary Where Query


}
