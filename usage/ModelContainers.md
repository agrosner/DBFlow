# Model Containers

Model Containers (```ModelContainer```) are **mirrors** to actual ```Model``` classes. Instead of parsing the data from a map, JSON object, or other form of data, converting the data into a ```Model``` and then saving it, we _directly_ save it to the database using a ```Model``` blueprint. This eliminates a layer of processing and is much more efficient.

Please note that they come with these restrictions:
  1. They must reference an existing ```Model``` table from the same database that you define. 
  2. You cannot ```SELECT``` from any ```ModelContainer``` class is it does not exist as a table. It implements ```Model``` as a convenience method to enable it to operate like a ```Model```.
  3. Make sure to reference the correct table as it will not know that it's the wrong table
  4. This is **NOT** a fully featured JSON/Map/Object parser inside an ORM database library. The column can have a ```@ContainerKey``` to specify a different key name from the ```ModelContainer``` than the column name.

Interesting features:
  1. Can be Foreign key fields
  2. ~~The data type (Map, JSONObject, and more) (if present as a Foreign Key) will be converted in a Model Container to save and retrieve it's data~~ (not yet).
  3. Can define your own container!

## Example

```java

JSONModel<TestObject> jsonModel = new JSONModel<TestObject>(json, TestObject.class);

// constructs an insert or update query based on the JSON contents
jsonModel.save(false);

// deletes a model based on the JSON contents
jsonModel.delete(false);

```
