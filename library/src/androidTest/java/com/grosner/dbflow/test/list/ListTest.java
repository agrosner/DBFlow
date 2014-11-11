package com.grosner.dbflow.test.list;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.grosner.dbflow.list.FlowCursorList;
import com.grosner.dbflow.list.FlowTableList;
import com.grosner.dbflow.runtime.transaction.ResultReceiver;
import com.grosner.dbflow.sql.language.Delete;
import com.grosner.dbflow.test.FlowTestCase;
import com.grosner.dbflow.test.utils.GenerationUtils;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ListTest extends FlowTestCase {
    @Override
    protected String getDBName() {
        return "list";
    }

    public void testTableList() {

        Delete.table(ListModel.class);

        List<ListModel> testModel1s = GenerationUtils.generateRandomModels(ListModel.class, 100);

        FlowTableList<ListModel> flowTableList = new FlowTableList<>(ListModel.class);

        assertTrue(flowTableList.size() == 100);

        assertTrue(flowTableList.containsAll(testModel1s));

        ListModel model1 = flowTableList.remove(0);

        assertTrue(flowTableList.size() == 99);

        assertTrue(flowTableList.add(model1));

        assertTrue(flowTableList.size() == 100);

        flowTableList.set(model1);

        flowTableList.clear();

        assertTrue(flowTableList.size() == 0);
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


    public void testCursorList() {

        Delete.table(ListModel.class);

        final List<ListModel> testModel1s = GenerationUtils.generateRandomModels(ListModel.class, 50);

        FlowCursorList<ListModel> flowCursorList = new FlowCursorList<>(true, ListModel.class);

        TestModelAdapter modelAdapter = new TestModelAdapter(flowCursorList);

        assertTrue(testModel1s.size() == modelAdapter.getCount());

        flowCursorList.fetchAll(new ResultReceiver<List<ListModel>>() {
            @Override
            public void onResultReceived(List<ListModel> models) {
                assertTrue(models.size() == testModel1s.size());
            }
        });

    }
}
