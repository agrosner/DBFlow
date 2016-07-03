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

`TypeConverter` do _not_ support:
  1. Any Parameterized fields _except_ `ForeignKeyContainer` and `ModelContainer` fields.
  2. `List<T>`, `Map<T>`, etc. Best way to fix this is to create a separate table [relationship](/usage2/Relationships.md)
  3. Conversion from one type-converter to another (i.e `JSONObject` to `Date`). The first parameter of `TypeConverter` is the value of the type as if it was a primitive/boxed type.
  4. Conversion from custom type to `Model`, or `Model` to a supported type.
  5. The custom class _must_ map to a non-complex field such as `String`, numbers, `char`/`Character` or `Blob` 

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
