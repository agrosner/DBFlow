# Tables as Lists


In this library, we enable easy list and adapter-based operations using ```FlowCursorList``` and ```FlowTableList```. Both 
of these correspond (in order) to ```BaseAdapter``` and ```List<? extends Model>```.

## Cursor List

A read-only cursor-backed object that provides caching of retrieved models used to display in a list. 
It contains methods very similiar to ```BaseAdapter``` such as ```getCount()``` and ```getItem(position)```. 

This class becomes useful when we have a very large dataset that we wish to display on screen without killing memory, or
keep memory usage to a slim minimum by only loading items we need. This class is similar to ```CursorAdapter``` except it does
not extend any adapter class and provides a simple yet effective API for retrieving and converted data from the database with ease.

```java

  private class TestModelAdapter extends BaseAdapter {
    private FlowCursorList<TestModel1> mFlowCursorList;

    public TestModelAdapter() {
  
    // retrieve and cache rows that have a name like pasta%
      mFlowCursorList = new FlowCursorList<>(true, TestModel.class, 
        Condition.column(TestModel1$Table.NAME).like("pasta%"));
    }

    @Override
    public int getCount() {
      return mFlowCursorList.getCount();
    }

    @Override
    public TestModel1 getItem(int position) {
      return mFlowCursorList.getItem(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      // Implement here
    }
  }

```

## Table List

A ```FlowTableList``` is java ```List``` implementation of managing a database table. 
All modifications affect the table in real-time. 
All modifications, by default, are immediate. 
If you wish to not run these on the main thread, call ```flowTableList.setTransact(true)```. 
Internally its backed by a ```FlowCursorList``` to include all of it's existing functionality. 

```java

FlowTableList<TestModel1> flowTableList = new FlowTableList<TestModel1>(TestModel1.class);

// Deletes from the table and returns the item
TestModel1 model1 = flowTableList.remove(0);

// Saves the item back into the DB, updates if it already exists
flowTableList.add(model1);

// Updates the item in the DB
flowTableList.set(model1);

// Clears the whole table
flowTableList.clear();
```
