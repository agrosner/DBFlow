# Condition & ConditionQueryBuilder

```Condition``` is a class that represents a condition statement within a SQL statement such as:

```sql

name = 'Test'

name = 'Test'

name LIKE '%Test%'

name != 'Test'

salary BETWEEN 15000 AND 40000

name IN('Test','Test2','TestN')

```

### How to use ```Condition```

The ```Condition``` class can specify:
  1. columnName: the name of the column in the database. It is strongly recommended to use the 
  column name from the generated ```$Table``` class for a ```Model``` as this will save you defining constant strings for the column names.
  2. operator: The valid SQL operator. Not "space separated" such that specifying ```operator("=")``` yields ```columnName=value```.
  3. value: The value of the data as represented by the ```Model```. This value will be converted into its database value when appending itself to a ```ConditionQueryBuilder```.
  4. post argument: The end of a condition, such as a ```Collate```
  5. separator (for within a ```ConditionQueryBuilder```): Specifies an optional SQLite condition separator that the ```ConditionQueryBuilder``` uses to separate its ```Condition```
