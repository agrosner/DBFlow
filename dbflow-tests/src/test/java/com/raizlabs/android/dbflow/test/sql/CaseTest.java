package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.language.BaseQueriable;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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

        BaseQueriable<CaseModel> queriable = SQLite.select(CaseModel_Table.customerId,
                CaseModel_Table.firstName,
                CaseModel_Table.lastName,
                SQLite.caseWhen(CaseModel_Table.country.eq("USA"))
                        .then("Domestic")
                        ._else("Foreign").end("CustomerGroup")).from(CaseModel.class);

        assertEquals("SELECT `customerId`,`firstName`,`lastName`, CASE WHEN `country`='USA' " +
                "THEN 'Domestic' ELSE 'Foreign' END `CustomerGroup` FROM `CaseModel`", queriable.getQuery().trim());

        Delete.table(CaseModel.class);
    }
}
