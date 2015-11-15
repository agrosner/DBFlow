# Type Converters
Type converters enable fields within a `Model` that are not necessarily database-typed. They convert the value of the field into a database type in the way that you can define. They also define the way that when a `Model` is loaded, we recreate the field using the converter. _Note_: `TypeConverter` can only be regular `Column`, not `PrimaryKey` or `ForeignKey` due  to the non-determinate mapping of data.

These converters are shared across all databases.

If we specify the model value as a `Model` class then, there may be some very unexpected behavior for fields that are defined as `Column.FOREIGN_KEY`

Here is the implementation of `LocationConverter`, converting Locations into Strings:

```java

  // First type param is the type that goes into the database
  // Second type param is the type that the model contains for that field.
  @com.raizlabs.android.dbflow.annotation.TypeConverter
  public class LocationConverter extends TypeConverter<String,Location> {

    @Override
    public String getDBValue(Location model) {
        return model == null ? null : String.valueOf(model.getLatitude()) + "," + model.getLongitude();
    }

    @Override
    public Location getModelValue(String data) {
        String[] values = data.split(",");
        if(values.length < 2) {
            return null;
        } else {
            Location location = new Location("");
            location.setLatitude(Double.parseDouble(values[0]));
            location.setLongitude(Double.parseDouble(values[1]));
            return location;
        }
    }
  }
```

To use the `LocationConverter`, using TypeConverters we simply add the class as a field in our table:

```java

@Table(...)
public class SomeTable extends BaseModel {


  @Column
  Location location;

}
```

## Column Specific TypeConverters
As of 3.0, `TypeConverter` can be used on a column-by-column basis.

```java

@Table(...)
public class SomeTable extends BaseModel {


  @Column(typeConverter = SomeTypeConverter.class)
  Location location;

}
```

_NOTE_: `enum` classes that want to change from the default enum conversion (from enum to String), you _must_ define a Custom Type Converter for the column.
