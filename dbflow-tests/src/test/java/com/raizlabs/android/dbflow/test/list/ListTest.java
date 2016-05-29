package com.raizlabs.android.dbflow.test.list;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.utils.GenerationUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class ListTest extends FlowTestCase {

    FlowQueryList<ListModel> modelList;

    @Before
    public void setupTest() {
        Delete.table(ListModel.class);
        modelList = new FlowQueryList<>(SQLite.select().from(ListModel.class));
    }

    @After
    public void deconstructTest() {
        Delete.table(ListModel.class);
    }

    @Test
    public void testTableList() {

        List<ListModel> testModel1s = GenerationUtils.generateRandomModels(ListModel.class, 100);

        FlowManager.getDatabaseForTable(ListModel.class)
                .executeTransaction(FastStoreModelTransaction
                        .saveBuilder(FlowManager.getModelAdapter(ListModel.class))
                        .addAll(testModel1s)
                        .build());

        modelList = new FlowQueryList<>(SQLite.select().from(ListModel.class));

        assertTrue(modelList.size() == 100);

        assertTrue(modelList.containsAll(testModel1s));

        ListModel model1 = modelList.remove(0);

        assertTrue(modelList.size() == 99);

        assertTrue(modelList.add(model1));

        assertTrue(modelList.size() == 100);

        modelList.set(model1);

        modelList.clear();

        assertTrue(modelList.size() == 0);
    }

    @Test
    public void testTableListEmpty() {
        ListModel listModel = new ListModel();
        listModel.setName("Test");
        modelList.add(listModel);

        assertTrue(modelList.size() == 1);
    }

    private class TestModelAdapter extends BaseAdapter {

        private FlowCursorList<ListModel> mFlowCursorList;

        public TestModelAdapter(FlowCursorList<ListModel> flowCursorList) {
            mFlowCursorList = flowCursorList;
        }

        @Override
        public int getCount() {
            return mFlowCursorList.getCount();
        }

        @Override
        public ListModel getItem(int position) {
            return mFlowCursorList.getItem(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }
    }

    @Test
    public void testCursorList() {

        final List<ListModel> testModel1s = GenerationUtils.generateRandomModels(ListModel.class, 50);

        FlowCursorList<ListModel> flowCursorList = new FlowCursorList<>(true, SQLite.select().from(ListModel.class));

        TestModelAdapter modelAdapter = new TestModelAdapter(flowCursorList);

        assertTrue(testModel1s.size() == modelAdapter.getCount());
        assertTrue(flowCursorList.getAll().size() == testModel1s.size());

    }
}
