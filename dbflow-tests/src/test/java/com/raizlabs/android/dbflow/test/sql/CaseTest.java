package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Test;

/**
 * Description:
 */
public class CaseTest extends FlowTestCase {

    @Test
    public void test_SQL() {
        Delete.table(CaseModel.class);

        CaseModel caseModel = new CaseModel();
        caseModel.customerId = 505;
        caseModel.firstName = "Andrew";
        caseModel.lastName = "Grosner";
        caseModel.country = "USA";
        caseModel.insert();

        caseModel = new CaseModel();
        caseModel.customerId = 506;
        caseModel.firstName = "Andrew";
        caseModel.lastName = "Grosners";
        caseModel.country = "Canada";
        caseModel.insert();



        Delete.table(CaseModel.class);
    }
}
