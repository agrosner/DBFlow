# Powerful Model Caching

Model caching in this library is very simple and is extremely extensible, accessible, and usable. 

A ```ModelCache``` is an interface to an actual cache that you use within a ```BaseCacheableModel```, ```FlowTableList```, ```FlowCursorList```, or 
can use it anywhere you wish. 

## Using a Cache

Using a cache is easy-peasy.

### BaseCacheableModel

Instead of extending ```BaseModel```, if your class extends ```BaseCacheableModel```, 
any modification to the Model is saved in the cache. When a query runs on the DB, it will store the instance of the ```BaseCacheableModel``` in the cache and the cache is soley responsible for handling memory.

The default cache is a ```ModelLruCache```.
You can override ```getCacheSize()``` to tell the default ```ModelLruCache``` the size of its contents.

To use a custom cache, simply override:

```java

@Override
protected ModelCache<? extends BaseCacheableModel, ?> getBackingCache() {
        return new MyCustomCache<>();
}

```

#### FlowCursorList + FlowTable List

With a ```ModelCache```, the ```FlowCursorList``` and ```FlowTableList``` are much more powerful than before. 
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
    public void addModel(Long id, ModelClass model) {
        synchronized (getCache()) {
            getCache().put(id, model);
        }
    }

    @Override
    public ModelClass removeModel(Long id) {
        ModelClass model = null;
        synchronized (getCache()) {
            model = getCache().remove(id);
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
    public ModelClass get(Long id) {
        return id == null ? null : getCache().get(id);
    }
}


```

