# Content Provider Generation

This library includes a very fast, easy way to use ```ContentProvider```! 
Using annotations, you can generate ```ContentProvider``` with ease.


## Getting Started

This feature is largely based off of [schematic](https://github.com/SimonVT/schematic), while leveraging DBFlow's power.

### Placeholder ContentProvider

In order to define a ```ContentProvider```, you must define a placeholder class:


```java

@ContentProvider(authority = TestContentProvider.AUTHORITY,
        databaseName = TestDatabase.NAME,
        baseContentUri = TestContentProvider.BASE_CONTENT_URI)
public class TestContentProvider {

    public static final String AUTHORITY = "com.raizlabs.android.dbflow.test.provider";

    public static final String BASE_CONTENT_URI = "content://";

}

### Adding To Manifest

```

In other applications or your current's ```AndroidManifest.xml``` add the provider:

```xml

<provider
            android:authorities="com.raizlabs.android.dbflow.test.provider"
            android:exported="true|false"
            android:name=".provider.TestContentProvider$Provider"/>

```

```android:exported```: setting this to true, enables other applications to make use of it. 
**True** is recommended for different application access.

### Adding endpoints into the data

Create an inner class with the ```@TableEndpoint``` annotation. It links up a query, insert, delete, and update 
to a specific table in the ```ContentProvider``` local database. 

Some recommendations:
  1. Name the inner class same as the table it's referencing
  2. Create a ```public static final String ENDPOINT = "{tableName}"``` field for reusability
  3. Create ```buildUri()``` method (see below) to aid in creating other ones.

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

There are much more detailed usages of the ```@ContentUri``` annotation. Those will be in a later section.

### Connect Model operations to the newly created ContentProvider

There are two kinds of ```Model``` that connect your application to a ContentProvider 
that was defined in your app, or another app. Extend these for convenience, however they are not required.

```BaseProviderModel```: Overrides all ```Model``` methods and performs them on the ```ContentProvider```

```BaseSyncableProviderModel```: same as above, except it will syncronize the data changes with the local app database as well!

