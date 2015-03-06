# Creating Tables and Database Structure

## Model & Creation

Add the ```@Table``` annotation and extend the ```BaseModel``` class. Also you may implement ```Model``` if you want to provide your own
custom implementation. As a convenience, the library provides the ```BaseModel``` class that contains the default implementation for ```Model```. 

Features:
  1. Nested ```Model``` defined as a ```Column.FOREIGN_KEY``` enables 1-1 relationships
  2. Any default java class is supported such as the primitives, boxed primitives, and ```String```.
  3. A non-default object with a ```TypeConverter``` is also saveable. 
  4. Multiple primary keys are supported (composite primary key)

A limitations on using ```Model```:
  1. All ```Model``` **MUST HAVE A DEFAULT CONSTRUCTOR**. We will load the data into the ```Model``` and use the default constructor.
  2. Subclassing works as one would expect: the library gathers all inherited fields annotated with ```@Column``` and count those as rows in the current class's database. 
    _Note: If you request an option to turn this off, I may add that in a further release._ 
  3. Column names default to the field name as a convenience, but if the name of your fields change you will need to specify the column name.
  4. All fields must be ```public``` or package private as the ```ModelAdapter``` class needs access to them. 
  _If anyone knows how to manipulate byte-code to allow private classes, let me know_
  5. All model class definitions must be top-level (in their own file) and ```public``` or package private.


### Sample Model

This is an example of a ```Model``` class with a primary key (at least one is required) and another field. 

```java
@Table
public class TestModel extends BaseModel {

    // All tables must have a least one primary key
    @Column(columnType = Column.PRIMARY_KEY)
    String name;

    // By default the column name is the field name
    @Column
    int randomNumber;

}

```

### SQLite Views

```ModelView``` are database view classes that operate similar to ```Model``` classes except they correspond to an Sqlite ```VIEW```.

They contain some SQLite and library limitations:
  1. The fields must be identical to the fields in the corresponding ```Model```.
  2. The ```query()``` clause must be defined in the ```@ModelView``` annotation
  3. You cannot **INSERT**, **UPDATE**, or **DELETE**. They are read-only in SQLite. 
  Any call to ```insert(async)```, ```update(async)```, ```save(async)```, or ```delete(async)``` will throw 
  an ```InvalidSqlViewOperationException```
  4. Cannot have any kind of keys, primary or foreign, thus nested ```Model``` objects will not work.
  
```BaseModelView``` implements ```Model``` so that it can be used in other instances on equal footing. 

#### Example

Given a standard ```Model```:

```java

@Table
public class AModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY)
    String name;

    @Column
    long time;

    @Column(columnType = Column.FOREIGN_KEY,
            references = {@ForeignKeyReference(columnType = String.class, columnName = "otherModel", foreignColumnName = "name")})
    OtherModel model;

    @Column
    Date date;

}


```

We can create  ```VIEW``` based off of it as such:

```java

@ModelView(query = "SELECT time from AModel where time > 0")
public class AModelView extends BaseModelView<AModel> {

    @Column
    long time;
}

```

Unfortunately, for now, we cannot use the generated ```$Table``` classes within the annotation as the compiler will not understand the variable passed.

## Utilizing a Relational Database

In this library we currently only officially support 1-1 relationships using the ```Column.FOREIGN_KEY``` property on the ```@Column``` annotation.
For 1 to many, we suggest using lazy-loading for maximum performance. 

## One To One

Use the ```ColumnType.FOREIGN_KEY``` to enable **one-to-one** relationships. For example:

```java

  @Column(columnType = Column.FOREIGN_KEY, 
    references = {@ForeignKeyReference(columnName = "columnInTable", columnType = String.class, foreignColumnName = "columnInForeignKeyTable"), 
                 @ForeignKeyReference(columnName = "column2InTable", columnType = String.class, foreignColumnName = "column2InForeignKeyTable")})
  SomeOtherTable object;

```

### Foreign Key Container

We strongly prefer you use a ```ForeignKeyContainer``` and ```saveForeignKeyModel() = false``` for maximum performance gain.

```saveForeignKeyModel()``` if true, whenever the current table is saved, the foreign key object is saved. For maximum performance gain and situations where we are saving many items with same reference, this becomes inefficient over time. Setting this to false results in you having to manage it yourself.

```java

  @Column(columnType = Column.FOREIGN_KEY, 
    references = {@ForeignKeyReference(columnName = "columnInTable", columnType = String.class, foreignColumnName = "columnInForeignKeyTable"), 
                 @ForeignKeyReference(columnName = "column2InTable", columnType = String.class, foreignColumnName = "column2InForeignKeyTable")},
    saveForeignKeyModel = false)
  ForeignKeyContainer<SomeOtherTable> object;

```

To lazy-retrieve the ```Model```, call ```object.toModel()```. 

### ForeignKeyReference

Note that the number of ```@ForeignKeyReference``` must match the number of primary keys in the foreign table. 

```@ForeignKeyReference``` describes three fields:
  1. columnName: The name of the current table's column for this field
  2. columnType: The type of the field, must be the same between the current table and foreign table.
  3. foreignColumnName: The name of the column in the foreign table

In the original example, this field will load the foreign field up automatically when this model is loaded from the DB. __When using the second example, we do not save the contained foreign key object to save on speed when we expect the reference to remain unchanged.__ Also the ```ForeignKeyContainer``` will hold onto data from the load until calling ```toModel()``` to prevent unnecessary DB lookups for maximum performance gain on load.

### One-to-Many

In order to support **one-to-many**, create a method to retrieve the list of ```Model``` objects from the DB using lazy-loading.

Following from our previous sample, the preferred method of retrieval is through **lazy-instantiation**. Adding this to the original class: 

```java

@Table
public class TestModel extends BaseModel {

    // All tables must have a least one primary key
    @Column(columnType = Column.PRIMARY_KEY)
    String name;

    // By default the column name is the field name
    @Column
    int randomNumber;

    private List<TestManyModel> testManyModels;

    public List<TestManyModel> getTestManyModels() {
       if(testManyModels == null) {
         testManyModels = Select.all(TestManyModels.class, 
                          Condition.column(TestManyModels$Table.TESTMODELNAME).is(name));
       }
       return testManyModels;
    }
}

```

