# Content Provider Generation
This library includes a very fast, easy way to use `ContentProvider`! Using annotations, you can generate `ContentProvider` with ease.

## Getting Started
This feature is largely based off of [schematic](https://github.com/SimonVT/schematic),
 while leveraging DBFlow's power.

### Placeholder ContentProvider
In order to define a `ContentProvider`, you must define it in a placeholder class:

```java

@ContentProvider(authority = TestContentProvider.AUTHORITY,
        database = TestDatabase.class,
        baseContentUri = TestContentProvider.BASE_CONTENT_URI)
public class TestContentProvider {

    public static final String AUTHORITY = "com.raizlabs.dbflow5.test.provider";

    public static final String BASE_CONTENT_URI = "content://";

}
```

or you can use the annotation in any class you wish. The recommended place would be in a `@Database` placeholder class. This is to simplify some of the declarations and keep it all in one place.

```java

@ContentProvider(authority = TestDatabase.AUTHORITY,
        database = TestDatabase.class,
        baseContentUri = TestDatabase.BASE_CONTENT_URI)
@Database(name = TestDatabase.NAME, version = TestDatabase.VERSION)
public class TestDatabase {

    public static final String NAME = "TestDatabase";

    public static final int VERSION = "1";

    public static final String AUTHORITY = "com.raizlabs.dbflow5.test.provider";

    public static final String BASE_CONTENT_URI = "content://";

}
```

### Adding To Manifest
In other applications or your current's `AndroidManifest.xml` add the **generated $Provider** class:

```xml

<provider
            android:authorities="com.raizlabs.dbflow5.test.provider"
            android:exported="true|false"
            android:name=".provider.TestContentProvider_Provider"/>
```

`android:exported`: setting this to true, enables other applications to make use of it.

**True** is recommended for outside application access.

**Note you must have at least one `@TableEndpoint` for it to compile/pass error checking**

### Adding endpoints into the data
There are two ways of defining `@TableEndpoint`:
1. Create an inner class within the `@ContentProvider` annotation.
2. Or Add the annotation to a `@Table` and specify the content provider class name (ex. TestContentProvider)

`@TableEndpoint`: links up a query, insert, delete, and update to a specific table in the `ContentProvider` local database.

Some recommendations:
1. (if inside a `@ContentProvider` class) Name the inner class same as the table it's referencing
2. Create a `public static final String ENDPOINT = "{tableName}"` field for reusability
3. Create `buildUri()` method (see below) to aid in creating other ones.

To define one:

```java

@TableEndpoint(ContentProviderModel.ENDPOINT)
public static class ContentProviderModel {

    public static final String ENDPOINT = "ContentProviderModel";

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = Uri.parse(BASE_CONTENT_URI + AUTHORITY).buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    @ContentUri(path = ContentProviderModel.ENDPOINT,
            type = ContentUri.ContentType.VND_MULTIPLE + ENDPOINT)
    public static Uri CONTENT_URI = buildUri(ENDPOINT);

}
```

or via the table it belongs to

```java


@TableEndpoint(name = ContentProviderModel.NAME, contentProvider = ContentDatabase.class)
@Table(database = ContentDatabase.class, tableName = ContentProviderModel.NAME)
public class ContentProviderModel extends BaseProviderModel<ContentProviderModel> {

    public static final String NAME = "ContentProviderModel";

    @ContentUri(path = NAME, type = ContentUri.ContentType.VND_MULTIPLE + NAME)
    public static final Uri CONTENT_URI = ContentUtils.buildUriWithAuthority(ContentDatabase.AUTHORITY);

    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String notes;

    @Column
    String title;

    @Override
    public Uri getDeleteUri() {
        return TestContentProvider.ContentProviderModel.CONTENT_URI;
    }

    @Override
    public Uri getInsertUri() {
        return TestContentProvider.ContentProviderModel.CONTENT_URI;
    }

    @Override
    public Uri getUpdateUri() {
        return TestContentProvider.ContentProviderModel.CONTENT_URI;
    }

    @Override
    public Uri getQueryUri() {
        return TestContentProvider.ContentProviderModel.CONTENT_URI;
    }
}
```

There are much more detailed usages of the `@ContentUri` annotation. Those will be in a later section.

### Connect Model operations to the newly created ContentProvider
There are two kinds of `Model` that connect your application to a ContentProvider that was defined in your app, or another app. Extend these for convenience, however they are not required.

`BaseProviderModel`: Overrides all `Model` methods and performs them on the `ContentProvider`

`BaseSyncableProviderModel`: same as above, except it will synchronize the data changes with the local app database as well!

#### Interacting with the Content Provider
You can use the `ContentUtils` methods:

```java

ContentProviderModel contentProviderModel = ...; // some instance

int count = ContentUtils.update(getContentResolver(), ContentProviderModel.CONTENT_URI, contentProviderModel);

Uri uri = ContentUtils.insert(getContentResolver(), ContentProviderModel.CONTENT_URI, contentProviderModel);

int count = ContentUtils.delete(getContentResolver(), someContentUri, contentProviderModel);
```

**Recommended** usage is extending `BaseSyncableProviderModel` (for inter-app usage) so the local database contains the same data. Otherwise `BaseProviderModel` works just as well.

```java

MyModel model = new MyModel();
model.id = 5;
model.load(); // queries the content provider

model.someProp = "Hello"
model.update(false); // runs an update on the CP

model.insert(false); // inserts the data into the CP
```

## Advanced Usage
### Notify Methods
You can define `@Notify` method to specify a custom interaction with the  `ContentProvider` and return a custom `Uri[]` that notifies the contained `ContentResolver`. These methods can have any valid parameter from the `ContentProvider` methods.

Supported kinds include:
1. Update
2. Insert
3. Delete

#### Example

```java

@Notify(method = Notify.Method.UPDATE,
paths = {}) // specify paths that will call this method when specified.
public static Uri[] onUpdate(Context context, Uri uri) {

  return new Uri[] {
    // return custom uris here
  };
}
```

### ContentUri Advanced
#### Path Segments
Path segments enable you to "filter" the uri query, update, insert, and deletion by a specific column and a value define by '#'.

To specify one, this is an example `path`

```java

path = "Friends/#/#"
```

then match up the segments as:

```java

segments = {@PathSegment(segment = 1, column = "id"),
    @PathSegment(segment = 2, column = "name")}
```

And to put it all together:

```java

@ContentUri(type = ContentType.VND_MULTIPLE,
path = "Friends/#/#",
segments = {@PathSegment(segment = 1, column = "id"),
    @PathSegment(segment = 2, column = "name")})
public static Uri withIdAndName(int id, String name) {
  return buildUri(id, name);
}
```
