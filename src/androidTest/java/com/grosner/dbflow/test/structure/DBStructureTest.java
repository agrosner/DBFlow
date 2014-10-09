package com.grosner.dbflow.test.structure;

import android.location.Location;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.structure.Column;
import com.grosner.dbflow.structure.ColumnType;
import com.grosner.dbflow.test.FlowTestCase;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class DBStructureTest extends FlowTestCase {

    @Override
    protected void modifyConfiguration(DBConfiguration.Builder builder) {
        builder.foreignKeysSupported();
    }

    @Override
    protected String getDBName() {
        return "dbstructure";
    }

    // region Test Primary Where Query

    public void testPrimaryWhereQuery() {
        ConditionQueryBuilder<TestPrimaryWhere> primaryWhere = mManager.getStructure().getPrimaryWhereQuery(TestPrimaryWhere.class);
        assertEquals(primaryWhere.getQuery(), "location = ? AND name = ?");
    }

    private static class TestPrimaryWhere extends TestModel1{
        @Column(@ColumnType(ColumnType.PRIMARY_KEY))
        private Location location;
    }

    // endregion Test Primary Where Query


}
