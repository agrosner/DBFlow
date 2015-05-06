# Powerful Model Caching

Model caching in this library is very simple and is extremely extensible, accessible, and usable.

A ```ModelCache``` is an interface to an actual cache that you use within a ```BaseCacheableModel```, ```FlowQueryList```, ```FlowCursorList```, or
can use it anywhere you wish.

## Using a Cache

Using a cache is easy-peasy.

### BaseCacheableModel

Instead of extending ```BaseModel```, if your class extends ```BaseCacheableModel```,
any modification to the Model is saved in the cache. When a query runs on the DB, it will store the instance of the ```BaseCacheableModel``` in the cache and the cache is soley responsible for handling memory.

**Note** if you run a ```SELECT``` with columns specified, it may cache partial ```Model``` classes.
The default cache is a ```ModelLruCache```.
You can override ```getCacheSize()``` to tell the default ```ModelLruCache``` the size of its contents.

To use a custom cache, simply override:

```java

@Override
protected ModelCache<? extends BaseCacheableModel, ?> getBackingCache() {
        return new MyCustomCache<>();
}

```

#### FlowCursorList + FlowQueryList

With a ```ModelCache```, the ```FlowCursorList``` and ```FlowQueryList``` are much more powerful than before.
You can now decide how to cache models in these classes by overriding:

```java

@Override
protected ModelCache<? extends BaseCacheableModel, ?> getBackingCache() {
        return new MyCustomCache<>();
}

```

#### Custom

You can create your own cache and use it wherever you want.

An example cache is using a copied ```LruCache``` from the support library:

```java

public class ModelLruCache<ModelClass extends Model> extends ModelCache<ModelClass, LruCache<Long, ModelClass>>{

    public ModelLruCache(int size) {
        super(new LruCache<Long, ModelClass>(size));
    }

    @Override
    public void addModel(Object id, ModelClass model) {
        if(id instanceof Number) {
            synchronized (getCache()) {
                Number number = ((Number) id);
                getCache().put(number.longValue(), model);
            }
        } else {
            throw new IllegalArgumentException("A ModelLruCache must use an id that can cast to" +
                                               "a Number to convert it into a long");
        }
    }

    @Override
    public ModelClass removeModel(Object id) {
        ModelClass model;
        if(id instanceof Number) {
            synchronized (getCache()) {
                model = getCache().remove(((Number) id).longValue());
            }
        }  else {
            throw new IllegalArgumentException("A ModelLruCache uses an id that can cast to" +
                                               "a Number to convert it into a long");
        }
        return model;
    }

    @Override
    public void clear() {
        synchronized (getCache()) {
            getCache().evictAll();
        }
    }

    @Override
    public void setCacheSize(int size) {
        getCache().resize(size);
    }

    @Override
    public ModelClass get(Object id) {
        if(id instanceof Number) {
            return getCache().get(((Number) id).longValue());
        } else {
            throw new IllegalArgumentException("A ModelLruCache must use an id that can cast to" +
                                               "a Number to convert it into a long");
        }
    }
}


```
