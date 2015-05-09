# Model Containers

Model Containers (```ModelContainer```) are **mirrors** to actual ```Model``` classes. Instead of parsing the data from a map, JSON object, or other form of data, converting the data into a ```Model``` and then saving it, we _directly_ save it to the database using a ```Model``` blueprint. This eliminates a layer of processing and is much more efficient.

Please note that they come with these restrictions:
  1. They must reference an existing ```Model``` table from the same database that you define.
  2. You cannot ```SELECT``` from any ```ModelContainer``` class is it does not exist as a table. It implements ```Model``` as a convenience method to enable it to operate like a ```Model```.
  3. Make sure to reference the correct table as it will not know that it's the wrong table
  4. This is **NOT** a fully featured JSON/Map/Object parser inside an ORM database library.

Interesting features:
  1. Can be Foreign key fields to enable **lazy-loading** of data such as ```ForeignKeyContainer```.
  2. Can define your own container!
  3. The column can have a ```@ContainerKey``` to specify a different key name from the ```ModelContainer``` than the column name.

## Example

```java

JSONModel<TestObject> jsonModel = new JSONModel<>(json, TestObject.class);

// constructs an insert or update query based on the JSON contents
jsonModel.save(false);

// deletes a model based on the JSON contents
jsonModel.delete(false);

```

## Supported Kinds

```MapModel```: takes a ```Map``` and operates it like a ```Model```.

```JSONModel````: uses ```JSONObject```

```JSONArrayModel```: all operations are on the contained ```JSONObject``` as one "Model". Each inner ```JSONObject``` is treated like a ```JSONModel```.

```ForeignKeyContainer```: enables lazy-loading of foreign key fields. Instead of querying for the ```Model``` when a parent ```Model``` loads, we can choose when to load the foreign key object using ```toModel()```.
