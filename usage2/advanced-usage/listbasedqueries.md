# ListBasedQueries

Converting a whole list at one chunk can be memory intensive. This lazily creates models from a `Cursor` for you and you can operate on it s if it's a list. It a acts like a list can be used in a for-loop:


```kotlin
(select from MyTable::class
    where ...) // some conditions
    .flowQueryList(database).use { list ->
          // list is just backed by an active cursor.
    }

(select from MyTable::class
    where ...) // some conditions
    .cursorList().use { list ->
     // ensure you close these when done, as they utilize active cursors :)
     // can use the list like a regular List
     for (model in list) {

     }

     list.forEach { printLn("$it") }
    }

```

**Note**: It's preferred within a `RecyclerView` to use the `QueryDataSource` with the Paging library, as this use can potentially lock the UI thread during heavy db usage.

## FlowCursorList

The `FlowCursorList` is simply a wrapper around a standard `Cursor`, giving it the ability to cache `Model`, load items at specific position with conversion, and refresh it's content easily.

The `FlowCursorList` provides these methods:

1. `list[index]` - loads item from `Cursor` at specified position
2. `refresh()` - re-queries the underlying `Cursor`. Use a `OnCursorRefreshListener` to get callbacks when this occurs.
3. `list.all` - converts it to a `List` of all items from the `Cursor`, no caching used.
4. `list.count` - returns count of `Cursor` or 0 if `Cursor` is `null`
5. `list.isEmpty` - returns if count == 0

## Flow Query List

This class is a much more powerful version of the `FlowCursorList`. It contains a `FlowCursorList`, which backs it's retrieval operations.

This class acts as `List` and can be used almost wherever a `List` is used.
