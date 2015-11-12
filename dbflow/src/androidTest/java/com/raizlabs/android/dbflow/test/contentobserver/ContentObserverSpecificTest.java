package com.raizlabs.android.dbflow.test.contentobserver;

import android.net.Uri;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;

/**
 * Description:
 */
public class ContentObserverSpecificTest extends FlowTestCase {

    public void testSpecificUris() {

        ContentObserverModel model = new ContentObserverModel();
        model.id = 5;
        model.name = "Something";
        model.somethingElse = "SomethingElse";
        ConditionGroup conditionGroup = FlowManager.getModelAdapter(ContentObserverModel.class).getPrimaryConditionClause(model);
        Uri uri = SqlUtils.getNotificationUri(ContentObserverModel.class, BaseModel.Action.DELETE,
                conditionGroup.getConditions().toArray(new SQLCondition[conditionGroup.getConditions().size()]));

        assertEquals(uri.getAuthority(), FlowManager.getTableName(ContentObserverModel.class));
        assertEquals(uri.getFragment(), BaseModel.Action.DELETE.name());
        assertEquals(Uri.decode(uri.getQueryParameter(Uri.encode(ContentObserverModel_Table.id.getQuery()))), "5");
        assertEquals(Uri.decode(uri.getQueryParameter(Uri.encode(ContentObserverModel_Table.name.getQuery()))), "Something");
    }

    public void testSpecificUrlNotifications() {

        Delete.table(ContentObserverModel.class);

        FlowContentObserver contentObserver = new FlowContentObserver();
        final Callable<Boolean>[] methodCalls = new Callable[4];
        for (int i = 0; i < methodCalls.length; i++) {
            methodCalls[i] = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return true;
                }
            };
        }
        final SQLCondition[][] conditions = new SQLCondition[4][2];
        contentObserver.addSpecificModelChangeListener(new FlowContentObserver.OnSpecificModelStateChangedListener() {
            @Override
            public void onModelStateChanged(Class<? extends Model> table, BaseModel.Action action, SQLCondition[] sqlConditions) {
                switch (action) {
                    case INSERT:
                        try {
                            conditions[0] = sqlConditions;
                            methodCalls[0].call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case UPDATE:
                        try {
                            conditions[1] = sqlConditions;
                            methodCalls[1].call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case SAVE:
                        try {
                            methodCalls[2].call();
                            conditions[2] = sqlConditions;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                    case DELETE:
                        try {
                            methodCalls[3].call();
                            conditions[3] = sqlConditions;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
        contentObserver.registerForContentChanges(getContext(), ContentObserverModel.class);
        ContentObserverModel model = new ContentObserverModel();
        model.id = 3;
        model.name = "Something";
        model.somethingElse = "SomethingElse";
        model.insert();

        await().atMost(10, TimeUnit.SECONDS).until(methodCalls[0]);

        // inserting
        assertTrue(conditions[0].length == 2);
        SQLCondition[] conditions1 = conditions[0];
        assertEquals(conditions1[0].columnName(), ContentObserverModel_Table.name.getQuery());
        assertEquals(conditions1[1].columnName(), ContentObserverModel_Table.id.getQuery());
        assertEquals(conditions1[0].value(), "Something");
        assertEquals(conditions1[1].value(), "3");

        model.name = "Something2";
        model.update();

        await().atMost(10, TimeUnit.SECONDS).until(methodCalls[1]);

        // updating
        assertTrue(conditions[1].length == 2);
        SQLCondition[] conditions2 = conditions[1];
        assertEquals(conditions2[0].columnName(), ContentObserverModel_Table.name.getQuery());
        assertEquals(conditions2[1].columnName(), ContentObserverModel_Table.id.getQuery());
        assertEquals(conditions2[0].value(), "Something2");
        assertEquals(conditions2[1].value(), "3");

        model.name = "Something3";
        model.save();
        await().atMost(5, TimeUnit.SECONDS).until(methodCalls[2]);

        // save
        assertTrue(conditions[2].length == 2);
        SQLCondition[] conditions3 = conditions[2];
        assertEquals(conditions3[0].columnName(), ContentObserverModel_Table.name.getQuery());
        assertEquals(conditions3[1].columnName(), ContentObserverModel_Table.id.getQuery());
        assertEquals(conditions3[0].value(), "Something3");
        assertEquals(conditions3[1].value(), "3");


        model.delete();
        await().atMost(5, TimeUnit.SECONDS).until(methodCalls[3]);

        // delete
        assertTrue(conditions[3].length == 2);
        SQLCondition[] conditions4 = conditions[3];
        assertEquals(conditions4[0].columnName(), ContentObserverModel_Table.name.getQuery());
        assertEquals(conditions4[1].columnName(), ContentObserverModel_Table.id.getQuery());
        assertEquals(conditions4[0].value(), "Something3");
        assertEquals(conditions4[1].value(), "3");


    }
}
