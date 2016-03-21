package com.raizlabs.android.dbflow.test.list;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.utils.GenerationUtils;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class ListTest extends FlowTestCase {

    @Test
    public void testTableList() {

        Delete.table(ListModel.class);

        List<ListModel> testModel1s = GenerationUtils.generateRandomModels(ListModel.class, 100);

        FlowQueryList<ListModel> flowQueryList = new FlowQueryList<>(ListModel.class);

        assertTrue(flowQueryList.size() == 100);

        assertTrue(flowQueryList.containsAll(testModel1s));

        ListModel model1 = flowQueryList.remove(0);

        assertTrue(flowQueryList.size() == 99);

        assertTrue(flowQueryList.add(model1));

        assertTrue(flowQueryList.size() == 100);

        flowQueryList.set(model1);

        flowQueryList.clear();

        assertTrue(flowQueryList.size() == 0);
    }

    @Test
    public void testTableListEmpty() {
        Delete.table(ListModel.class);

        FlowQueryList<ListModel> flowQueryList = new FlowQueryList<>(ListModel.class);
        ListModel listModel = new ListModel();
        listModel.setName("Test");
        flowQueryList.add(listModel);

        assertTrue(flowQueryList.size() == 1);

        Delete.table(ListModel.class);
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

        Delete.table(ListModel.class);

        final List<ListModel> testModel1s = GenerationUtils.generateRandomModels(ListModel.class, 50);

        FlowCursorList<ListModel> flowCursorList = new FlowCursorList<>(true, ListModel.class);

        TestModelAdapter modelAdapter = new TestModelAdapter(flowCursorList);

        assertTrue(testModel1s.size() == modelAdapter.getCount());
        assertTrue(flowCursorList.getAll().size() == testModel1s.size());

    }
}
