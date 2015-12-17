# Triggers, Indexes, and More
This section contains more advance usage of SQLite. These features are very useful and can be used to improve DB and app performance.

## Triggers
`Trigger` are actions that are automatically performed before or after some action on the database. For example, we want to log changes for all updates to the name on the `Friend` table.

```java

CompletedTrigger<Friend> trigger = Trigger.create("NameTrigger")
                                    .after().update(Friend.class, Friend_Table.name)
                                    .begin(
                                        SQLite.insert(FriendLog.class)
                                          .columnValues(FriendLog_Table.oldName.eq("old.Name"),
                                          FriendLog_Table.newName.eq("new.Name"), FriendLog_Table.date.eq(System.currentTimeMillis()));
 // starts a trigger
 trigger.enable();

 // stops a trigger
 trigger.disable();
```

## Indexes
`Index` are pointers to specific columns in a table that enable super-fast retrieval. The trade-off of using these is that the database size significantly increases, however if performance is more important, the tradeoff is worth it.

Indexes are defined using the `indexGroups()` property of the `@Table` annotation. These operate similar to how `UniqueGroup` work:
1. specify an `@IndexGroup`
2. Add the `@Index`
3. Build and an `IndexProperty` gets generated. This allows super-easy access to the index so you can enable/disable it with ease.

You can define as many `@IndexGroup` you want within a `@Table` as long as one field references the group. Also individual `@Column` can belong to any number of groups:

```java

@Table(database = TestDatabase.class,
       indexGroups = {
               @IndexGroup(number = 1, name = "firstIndex"),
               @IndexGroup(number = 2, name = "secondIndex"),
               @IndexGroup(number = 3, name = "thirdIndex")
       })
public class IndexModel2 extends BaseModel {

   @Index(indexGroups = {1, 2, 3})
   @PrimaryKey
   @Column
   int id;

   @Index(indexGroups = 1)
   @Column
   String first_name;

   @Index(indexGroups = 2)
   @Column
   String last_name;

   @Index(indexGroups = {1, 3})
   @Column
   Date created_date;

   @Index(indexGroups = {2, 3})
   @Column
   boolean isPro;
}
```

Using the generated `IndexProperty` you can use it within queries:

```java

SQLite.select()
  .from(IndexModel2.class)
  .indexedBy(IndexModel2_Table.firstIndex)
  .where(...); // do a query here.
```
