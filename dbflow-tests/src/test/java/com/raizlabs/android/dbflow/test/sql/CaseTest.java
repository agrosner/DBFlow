package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.sql.language.BaseQueriable;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Method;
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

        queriable = SQLite.select(CaseModel_Table.customerId,
                CaseModel_Table.firstName,
                CaseModel_Table.lastName,
                SQLite._case(CaseModel_Table.country)
                        .when("USA")
                        .then("Domestic")
                        ._else("Foreign").end("CustomerGroup")).from(CaseModel.class);

        assertEquals("SELECT `customerId`,`firstName`,`lastName`, CASE `country` WHEN 'USA' " +
                "THEN 'Domestic' ELSE 'Foreign' END `CustomerGroup` FROM `CaseModel`", queriable.getQuery().trim());


        Delete.table(CaseModel.class);
    }

    @Test
    public void test_caseProperty() {
        String query = SQLite._case(CaseModel_Table.country)
                .when(CaseModel_Table.firstName)
                .then(CaseModel_Table.lastName).getQuery();
        assertEquals("CASE `country` WHEN `firstName` THEN `lastName`", query.trim());
    }

    @Test
    public void test_emptyEndCase() {
        String query = Method.count(SQLite._case(CaseModel_Table.country)
                .when(CaseModel_Table.firstName)
                .then(CaseModel_Table.lastName).end()).getQuery();
        assertEquals("COUNT( CASE `country` WHEN `firstName` THEN `lastName` END )", query.trim());

    }
}
