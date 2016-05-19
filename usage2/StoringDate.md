# Storing Date

DBFlow provide few mechanisms to store date into the database. The difference of options
should not provide confusion but rather allow flexibility in what you decide is the best way
to store information.

## Storage types

SQLite doesn't have a storage class set aside for storing dates and/or times. But SQLite are capable of storing dates and times as TEXT, REAL and INTEGER.
DBFlow support two of the three types of date storage existents in the SQLite and they are TEXT and INTEGER values.

## How the storage works under the hood

Well, in your java code you has two ways to declare columns that will storage date values, those ways are declare
the attributes of your model class as String or as Date:

An example using String:

```java
@Table(database = AppDatabase.class)
public class Delivery extends BaseModel {

    @PrimaryKey
    String id;

    @Column
    String deliveryDate;

}
```

An example using Date:

```java
@Table(database = AppDatabase.class)
public class Delivery extends BaseModel {

    @PrimaryKey
    String id;

    @Column
    Date deliveryDate;

}
```

The difference between the examples is actually in how DBFlow perform the storing of the values getter from deliveryDate.
When a date is stored as String, the DBFlow just perform the insertion of the value as a type TEXT for the SQLite, because is allowed. But when we want to store deliveryDate as a Date, the DBFlow will convert this Date object to a INTEGER value that represents a unixexpoch time, doing something like:
 
```java 
long unixepoch = deliveryDate.getTime() / 1000;
SQLiteStatement insertStatement = "INSERT INTO (dalivieryDate) VALUES(" + unixepoch + ")";
```

You may be wondering why execute deliveryDate.getTime() / 1000 ? Well, this is necessary because we have a difference between the time unit of deliveryDate.getTime() and the time unit supported by SQLite. When we call deliveryDate.getTime() the value gived to us the **milliseconds** since January 1, 1970, 00:00:00 GMT represented by this Date object.
But unixepoch is the amount of **seconds** since January 1, 1970 without time zone and by running the code above we are getting from our date the **seconds** since January 1, 1970, 00:00:00 GMT represented by this Date object or simply a unixepoch that is the INTEGER format supported by SQLite.

## Why do we need this

We need this because if our dates aren't stored on those ways we'll be incapable to use the SQLite date and time functions because of our dates are stored in another way the SQLite will not be able to recognize this date and precess them for us.

## Retrieval

So let's say that we wanna retrieve all the deliveries orders from some day to check if all ordered deliveries are ok, and lets consider that our date inquestion is '15-02-2016 20:35:34'.

If you have stored your date values using String them you can query data by date doing something like the following code:

```java
public List<Delivery> getDeliveries(String comparisonDate) {
    new Select().from(Delivery.class).where(deliveryDate = ?, comparisonDate).querySingle();
}
```

If you have stored your date values using Date to query deliveries by date them you query will be a little different and probably will looks like:

```java
public List<Delivery> getDeliveries(String comparisonDate) {
    new Select().from(Delivery.class).where(datetime(deliveryDate, 'unixepoch', 'localtime') = ?, comparisonDate).querySingle();
}
```
 
If you wanna get those data and format the comparisonDate to a specific date time format your queries will looks like:

An example using String:

```java
public List<Delivery> getDeliveries(String comparisonDate) {
    new Select().from(Delivery.class).where(strftime('%Y-%m-%d', deliveryDate) = ?, comparisonDate).querySingle();
}
```

An example using Date:

```java
public List<Delivery> getDeliveries(String comparisonDate) {
    new Select().from(Delivery.class).where(strftime('%Y-%m-%d', datetime(deliveryDate, 'unixepoch', 'localtime') = ?, comparisonDate).querySingle();
}
```


In the above case we are using two SQLite functions and they are:
strftime() - The strftime() routine returns the date formatted according to the format string specified as the first argument.
datetime() - This function returns "YYYY-MM-DD HH:MM:SS".

At the datetime() function we have informed what column we want to get and them we have passed two more modifiers and they are:
unixepoch - A modifier used to specify that the column passed ate datetime() function was stored as a unixepoch value.
localtime - A modifier used to inform to the datetime() function that the user wanna the final result with as a date and time considering his timezone.

## Considerations
If you wanna see the full documentation of:
 * SQLite data types click [here](https://www.sqlite.org/datatype3.html).
 * SQLite data and time functions click [here](https://www.sqlite.org/lang_datefunc.html).
