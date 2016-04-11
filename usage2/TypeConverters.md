# Type conversion

When building out `Model` classes, you may wish to provide a different type of `@Column` that from the standard supported column types. To recap the standard column types include:
  1. `String`, `char`, `Character`
  2. All numbers types (primitive + boxed)
  3. `byte[]`/`Byte`
  4. `Blob` (DBFlow's version)
  5. `Date`/`java.sql.Date`
  6. Bools
  7. `Model` as `@ForeignKey`
  8. `ForeignKeyContainer` as `@ForeignKey`

## Define a TypeConverter

Defining a `TypeConverter` is quick and easy.

This example creates a `TypeConverter` for a field that is `JSONObject` and converts it to a `String` representation:

```java

@com.raizlabs.android.dbflow.annotation.TypeConverter
public class JSONConverter extends TypeConverter<String, JSONObject> {

    @Override
    public String getDBValue(JSONObject model) {
        return model == null ? null : model.toString();
    }

    @Override
    public JSONObject getModelValue(String data) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(data);
        } catch (JSONException e) {
          // maybe log this?
        } finally {
            return jsonObject;
        }
    }
}

```

Once this is defined, by using the annotation `@TypeConverter`, it is registered automatically accross all databases.

There are cases where you wish to provide multiple `TypeConverter` for same kind of field (i.e. `Date` with different date formats stored in a DB).

## TypeConverter for specific `@Column`

In DBFlow, specifying a `TypeConverter` for a `@Column` is as easy as `@Column(typeConverter = JSONConverter.class)`. What it will do is create the converter once for use only when that column is used.
