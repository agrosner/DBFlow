package com.raizlabs.android.dbflow.test.contentobserver;

import android.net.Uri;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.structure.BaseModel;
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
}
