# Storing Data

DBFlow provide a few mechanisms by which we store data to the database. The difference of options
should not provide confusion but rather allow flexibility in what you decide is the best way
to store information.

## Synchronous Storage

While generally saving data synchronous should be avoided, for small amounts of data
it has little effect.

```java

model.insert(); // inserts
model.update(); // updates
model.save(); // checks if exists, if true update, else insert.

```

Code like this should be avoided:

```java

for (int i = 0; i < models.size(), i++) {
  models.get(i).save();
}

```

Instead we should move onto `Transaction`.

## Transactions

Transactions are ACID in SQLite, meaning they either occur completely or not at all.
Using transactions significantly speed up the time it takes to store. So recommendation
you should use transactions whenever you can.

DBFlow supports two kinds of transactions: synchronous (blocking) vs. asynchronous. 

### Synchronous Transactions
