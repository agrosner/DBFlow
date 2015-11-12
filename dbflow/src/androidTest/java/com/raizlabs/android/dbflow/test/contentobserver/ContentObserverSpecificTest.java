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
        final boolean[] methodCalls = new boolean[4];
        final SQLCondition[][] conditions = new SQLCondition[4][2];
        contentObserver.addSpecificModelChangeListener(new FlowContentObserver.OnSpecificModelStateChangedListener() {
            @Override
            public void onModelStateChanged(Class<? extends Model> table, BaseModel.Action action, SQLCondition[] sqlConditions) {
                switch (action) {
                    case SAVE:
                        methodCalls[0] = true;
                        conditions[0] = sqlConditions;
                        break;
                    case DELETE:
                        methodCalls[1] = true;
                        conditions[1] = sqlConditions;
                        break;
                    case INSERT:
                        methodCalls[2] = true;
                        conditions[2] = sqlConditions;
                        break;

                    case UPDATE:
                        methodCalls[3] = true;
                        conditions[3] = sqlConditions;
                        break;
                }
            }
        });
        contentObserver.registerForContentChanges(getContext(), ContentObserverModel.class);
        ContentObserverModel model = new ContentObserverModel();
        model.id = 3;
        model.name = "Something";
        model.somethingElse = "SomethingElse";
        model.save();

        assertTrue(methodCalls[0]);
        assertTrue(conditions[0].length == 2);
        assertEquals(conditions[0][0].columnName(), ContentObserverModel_Table.name.getQuery());
        assertEquals(conditions[0][1].columnName(), ContentObserverModel_Table.id.getQuery());
    }
}
