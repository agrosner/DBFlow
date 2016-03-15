package com.raizlabs.android.dbflow.test.contentobserver;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jayway.awaitility.Duration;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class ContentObserverSpecificTest extends FlowTestCase {

    @Test
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

    @Test
    public void testSpecificUrlNotifications() {

        Delete.table(ContentObserverModel.class);

        FlowContentObserver contentObserver = new FlowContentObserver();
        MockOnModelStateChangedListener mockOnModelStateChangedListener = new MockOnModelStateChangedListener();
        contentObserver.addModelChangeListener(mockOnModelStateChangedListener);
        contentObserver.registerForContentChanges(RuntimeEnvironment.application, ContentObserverModel.class);
        ContentObserverModel model = new ContentObserverModel();
        model.id = 3;
        model.name = "Something";
        model.somethingElse = "SomethingElse";
        model.insert();

        await().atMost(Duration.FIVE_SECONDS).until(mockOnModelStateChangedListener.getMethodCalls()[0]);

        // inserting
        assertTrue(mockOnModelStateChangedListener.getConditions()[0].length == 2);
        SQLCondition[] conditions1 = mockOnModelStateChangedListener.getConditions()[0];
        assertEquals(conditions1[0].columnName(), ContentObserverModel_Table.name.getQuery());
        assertEquals(conditions1[1].columnName(), ContentObserverModel_Table.id.getQuery());
        assertEquals(conditions1[0].value(), "Something");
        assertEquals(conditions1[1].value(), "3");

        model.somethingElse = "SomethingElse2";
        model.update();

        await().atMost(Duration.FIVE_SECONDS).until(mockOnModelStateChangedListener.getMethodCalls()[1]);

        // updating
        assertTrue(mockOnModelStateChangedListener.getConditions()[1].length == 2);
        SQLCondition[] conditions2 = mockOnModelStateChangedListener.getConditions()[1];
        assertEquals(conditions2[0].columnName(), ContentObserverModel_Table.name.getQuery());
        assertEquals(conditions2[1].columnName(), ContentObserverModel_Table.id.getQuery());
        assertEquals(conditions2[0].value(), "Something");
        assertEquals(conditions2[1].value(), "3");

        model.somethingElse = "Something3";
        model.save();
        await().atMost(5, TimeUnit.SECONDS).until(mockOnModelStateChangedListener.getMethodCalls()[2]);

        // save
        assertTrue(mockOnModelStateChangedListener.getConditions()[2].length == 2);
        SQLCondition[] conditions3 = mockOnModelStateChangedListener.getConditions()[2];
        assertEquals(conditions3[0].columnName(), ContentObserverModel_Table.name.getQuery());
        assertEquals(conditions3[1].columnName(), ContentObserverModel_Table.id.getQuery());
        assertEquals(conditions3[0].value(), "Something");
        assertEquals(conditions3[1].value(), "3");


        model.delete();
        await().atMost(5, TimeUnit.SECONDS).until(mockOnModelStateChangedListener.getMethodCalls()[3]);

        // delete
        assertTrue(mockOnModelStateChangedListener.getConditions()[3].length == 2);
        SQLCondition[] conditions4 = mockOnModelStateChangedListener.getConditions()[3];
        assertEquals(conditions4[0].columnName(), ContentObserverModel_Table.name.getQuery());
        assertEquals(conditions4[1].columnName(), ContentObserverModel_Table.id.getQuery());
        assertEquals(conditions4[0].value(), "Something");
        assertEquals(conditions4[1].value(), "3");

    }
}
