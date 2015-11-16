# Triggers, Indexes, and More
This section contains more advance usage of SQLite. These features are very useful and can be used to improve DB and app performance.

## Triggers
`Trigger` are actions that are automatically performed before or after some action on the database. For example, we want to log changes for all updates to the name on the `Friend` table.

```java

CompletedTrigger<Friend> trigger = Trigger.create("NameTrigger")
                                    .after().update(Friend.class, Friend$Table.NAME)
                                    .begin(
                                        new Insert<FriendLog>(FriendLog.class)
                                          .columns(FriendLog$Table.OLDNAME, FriendLog$Table.NEWNAME, FriendLog$Table.DATE)
                                          .values("old.Name", "new.Name", System.currentTimeMillis())
                                          };
 // starts a trigger
 trigger.enable();

 // stops a trigger
 trigger.disable();
```

## Indexes
`Index` are pointers to specific columns in a table that enable super-fast retrieval. The trade-off of using these is that the database size significantly increases, however if performance is more important, the tradeoff is worth it.

```java

Index<Friend> index = new Index<>("index_friendName")
                      .on(Friend.class, Friend$Table.NAME);

// begins an index
index.enable();

// drops an index
index.disable();
```

### INDEXED BY
As of 1.5.1, the `INDEXED BY` clause is now supported. Using the previous `Index` statement, we can now utilize the power of indexes in a query.

```java

List<Friend> friends = new Select()
                      .from(Friend.class)
                      .indexedBy("index_friendName")
                      .where(Condition.column(Friend$Table.NAME).like("Andrew%")).queryList();
```
