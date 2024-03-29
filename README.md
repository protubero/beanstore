

Beanstore is a fast and versatile data store for Java with a low barrier to entry and an easy-to-use API. The goal of the project is to offer a serious alternative to conventional database systems for certain usage scenarios. Therefore it offers features such as transactions, data validation and migration. The project is always useful when data needs to be stored permanently and a database seems too heavy and inflexible. The natural limitations are that all data must fit into memory. Also, with very frequent changes, startup time could possibly become a factor.

Beanstore has a rich callback API, e.g. you can reject a transaction in callback code. This allows implementing custom validation logic. And the API makes it easy to create projections and aggregations of the data and keep them up to date.

The way data is maintained and stored means that every state of the data from the past can be reproduced. Beanstore includes two ways to take advantage of this. On the one hand, you can access the complete history of a single instance. On the other hand, you can create and evaluate a snapshot of any historical state.

Beanstore has a plugin API that allows third parties to offer additional data-related add-ons, e.g. for data validation, search engines, etc. Beanstore comes with a Lucene-based in-memory search engine and a validation plugin based on Java Bean Validation.


## Table of contents

- [Versioning](#versioning)
- [Recent Releases](#recent-releases)
- [Maven Dependency](#maven-dependency)
- [Building from source](#building-from-source)
- [Quickstart](#quickstart)
- [Entities](#entities)
- [Values](#values)
- [Build a store](#build-a-store)
- [Kryo Configuration](#kryo-configuration)
- [Transactions](#transactions)
  * [Optimistic Locking](#optmistic-locking)
  * [Locked Store](#locked-store)
  * [Transaction Listener](#transaction-listener)
- [Query Store](#query-store)
- [Migration](#migration)
- [Plugins](#plugins)
  * [Bean Validation Plugin](#bean-validation-plugin)
  * [Fulltext Search Plugin](#fulltext-search-plugin)
  * [Transaction History Plugin](#transaction-history-plugin)
  * [Transaction Log Plugin](#transaction-log-plugin)
- [Advanced Topics](#advanced-topics)
  * [Plugin API](#plugin-api)
  * [PropertyBeanSerializer](#propertybeanserializer)
  * [Kryo Configuration Framework Support](#kryo-configuration-framework-support)
  * [Close Store](#close-store)
- [Appendix](#appendix)
  * [Standard Data Types](#standard-data-types)
  

## Versioning

| Version   | Description |
| ------------- | ------------- |
| 0.8.x  | To be used for testing the library and possibly for small, time-limited personal projects. Undocumented breaking changes can occur in any release, no migration will be provided  |
| 0.9.x  | The library is considered mature enough to be used for small private projects. Breaking changes should be rare and well documented. Migrations, e.g. for changes of the persistence file format, will be provided.  |
| 1.x.x  | For all kind of projects. Semantic Versioning will be used. Migrations, e.g. for changes of the persistence file format, will be provided. Plugins with external dependencies like the Fulltext Search Plugin will be moved to a separate project |

# Recent releases

* [0.8.5](https://github.com/protubero/beanstore/releases/tag/beanstore-0.8.5) - warming up release actions after extensive refactoring.


## Maven Dependency

To use the latest Beanstore release in your application, use this dependency entry in your `pom.xml`:

```xml
<dependency>
   <groupId>de.protubero</groupId>
   <artifactId>beanstore</artifactId>
   <version>0.8.5</version>
</dependency>
```

## Building from source

Building Beanstore from source requires JDK11+ and Maven. To build all artifacts, run:

```
mvn clean && mvn install
```


## Quickstart

jumping ahead to show how the library can be used:

First create a Data Bean Class

```java

@Entity(alias = "todo")
public class ToDo extends AbstractEntity {

	private String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
```

Then create and configure the Beanstore builder, register the data bean class, build the BeanStore

```java
KryoConfiguration kryoConfig = KryoConfiguration.create();
KryoPersistence persistence = KryoPersistence.of(new File("/path/to/file.bst"), kryoConfig);
BeanStoreBuilder builder = BeanStoreBuilder.init(persistence);
builder.registerEntity(ToDo.class);

BeanStore store = builder.build();
```

Add some data using a transaction
```java
var tx = store.transaction();		
ToDo newToDo = tx.create(ToDo.class);
newToDo.setText("Hello World");
tx.execute();
```

Query data - read list of todos
```java
var allToDos = store.snapshot().entity(ToDo.class).stream().collect(Collectors.toList());
allToDos.forEach(System.out::println);
```

## Beanstore characteristics

Other libraries try to apply persistence to ordinary Java objects. Bean Store takes a different approach. The instances in the bean store are completely under the control of the library. Instances in the store cannot be changed by setting properties. Changes must be described as transactions and left to the store to apply. Instances are immutable. Any changes made by a transaction result in a copy being created. Each execution of a transaction creates a new, immutable state of the data (snapshot).

Beanstore transactions describe changes to a set of “Java Beans”. These changes are simultaneously applied to the beans and stored in a log-structured file. The next time the application is started, all changes will be replayed to restore the last store state. If you've ever heard of event sourcing, you're already familiar with the concept. The Java Beans specification requires that beans be serializable through Java Object Serialization. Instead, we use Kryo as a serialization framework. And we require that the beans have no default values, which effectively prevents the use of native types like int and boolean. We will refer to them as “data beans” throughout this documentation. We won't use the POJO term because the classes still have to follow the remaining Java Bean Rules (getters/setters and no-arg constructor).

## Entities

Each store is a set of entities that resemble tables in a relational database. You can choose whether such an entity is represented by a data bean, i.e. whether there is an associated Java class. If not, beanstore uses a generic map-like representation. You can switch between the two representations at any time. At the persistence level, instances are nothing more than sets of key/value pairs. It is therefore possible to decide at the time of loading the data for each type whether it will be mapped as maps or as 'data beans'. In the latter case, the bean classes define some kind of schema for the data.

Each entity has a unique alias. For the data beans, this is determined using the _Entity_ annotation on the Java class.

A BeanStore entity class must extend the _AbstractEntity_ class. It must have a no-argument constructor and expose its properties through setters and getters - as required by the Java Beans specification. Deviating from the specification, it does not have to be serializable in the sense of the _Java Object Serialization_. But it has to be kryo-serializable.

In general, a store is simply a list of instances of different types. A single instance consists of a set of key/value pairs. Each instance has a unique _id_ (long), which is assigned by the store itself. And it has a _versionId_ (int) that is incremented with every change.

The `AbstractEntity` class implements a *Map* interface to make the bean properties accessible in a *map-ish* way. The methods of the map interface are only partly supported, unsupported operations are: *containsValue*, *remove*, *clean*, *values*.

Many instances of the same type can exist with the same identity (id). The instances themselves are immutable. Each change results in the creation of a new copy with an incremented _versionId_.
  
The BeanStore is designed as a store of immutable objects! _Stored beans_ will throw an exception if you call a setter method. 


```java
@Entity(alias = "todo")
public class ToDo extends AbstractEntity {

	private String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
```

> [!WARNING]  
> Do not declare your bean classes *final*. BeanStores uses ByteBuddy to dynamically creates subclasses of your beans. Which will not work with final classes.


## Values

All values ​​should be instances of immutable classes. If the value's class does not guarantee immutability, you must still use it as if it were immutable. To be more specific: Never do `instanceX.getValueY().setPropertyZ(...)`, instead always set newly constructed values `instanceX.setValueY(newValueObj)`

Beanstore must know how to serialize a value. To be more specific: as we use Kryo for serialization, Kryo must know how to handle all values. You'll find more information on that topic in the sections below.

## Build a store

The creation of a store follows the builder pattern. The configuration of the store builder determines 

 * how the data is __persisted__. Currently the only way to do so is to let Kryo serialize the data and append it to a file.
 * which __entities__ are in the store and what __type__ they are. Is the entity a map type or is it represented by a java class?
 * how a new store is __initialized__, i.e. which data is automatically created when a store is started for the first time.

Some of the advanced features have their own section in the documentation:
- [Migrations](#migrations)
- [Plugins](#plugins)

The typical builder setup code looks like this:
```java
KryoConfiguration kryoConfig = KryoConfiguration.create();
KryoPersistence persistence = KryoPersistence.of(new File(someDir, "file.bst"), kryoConfig);
BeanStoreBuilder builder = BeanStoreBuilder.init(persistence);

// register a class based entity
builder.registerEntity(ToDo.class);

// register a map based entity
builder.registerMapEntity("note");

// create initial data. This code is only called when the store is newly created
builder.initNewStore(tx -> {
	var todo = tx.create(ToDo.class);
	todo.setText("Write more tests");

	Note note = tx.create("note");
	note.put("text", "My Text");
});
		
// Create the BeanStore,
BeanStore store = builder.build();
```

Building a store is a process consisting of the following steps:

* Read the file from the beginning to the end, extracting all change events
* Build up a interim store with map based entities only by applying the change events one after the other
* Applying migrations or, if the store is new, apply the store init code
* The interim store is transformed into the actual store. For bean based entities the respective maps are replaced by bean instances.

At this point, various situations can arise that the library must take action on

* The loaded data contains instances of entities which were not registered with the builder
* The loaded data contains properties which are not available in the registered bean class

In the first case, Beanstore will raise an error. However, you can instruct the library to automatically create the missing entities in these cases by setting the builder flag _autoCreateEntities_ to true. This will automatically create map based entities.

The second case cannot be handled. Registered beans must have a complete set of properties. Otherwise an exception will be thrown.


> [!NOTE]  
> A challenge of the persistence approach is making data accessible. E.g. if you find a file on your system that you know contains Beanstore data. How can you check the data without havin access to the application that wrote the data? The _autoCreateEntities_ flag allows data to be loaded without knowing the entities beforehand. Note that you will still need access to any custom Kryo serializers that may have been used to write the data!

## Kryo Configuration

For persistent storage, all data is serialized using the [Kryo](https://github.com/EsotericSoftware/kryo) library. Many data types work out-of-the-box. These are listed in the appendix on [standard data types](#standard-data-types).

All non-standard data types must be explicitly registered. You can write your own serializer or use one of the ones Kryo provides.

```java
// this line is sufficient if no other than the standard types are used
KryoConfiguration kryoConfig = KryoConfiguration.create();

// In case you have a custom value class 'Coordinates' you have
// to implement and register a custom Kryo Serializer for it:
kryoConfig.register(Coordinates.class, new MyValueClassSerializer(), 356);

// Beanstore offers support for using your own value classes without having to write a serializer by the PropertyBeanSerializer class.
kryoConfig.register(Car.class, PropertyBeanSerializer.class, 357);

```

Beanstore comes with one implementation of the Kryo Serializer interface to simplify the serialization of your own value classes. The PropertyBeanSerializer class 


> [!NOTE]  
> Kryo IDs have to be greater than 100!


## Transactions

The transactions are processed strictly sequentially.

BeanStore has four options to execute transactions. Some of them blocking, some of them non-blocking.

Blocking
* BeanStore.locked(Consumer&lt;BeanStoreTransactionFactory&gt; consumer)
* BeanStore.transaction().execute()

Non-Blocking
* BeanStore.lockedAsync(Consumer&lt;BeanStoreTransactionFactory&gt; consumer)
* BeanStore.transaction().executeAsync()

The BeanStore always applies the transactions to the store data *ony by one*. This is achieved by using a transaction queue. All new transactions are enqueued, the store takes the transactions one after another from the queue. The *locked* variants call the callback code just when the store pulls this 'transaction factory' from the queue. The factory is then given the possibility to create the transactions. This is, how *pessimistic locking* is implemented. E.g. if you want to update all instances of an entity at once, like you would do with a SQL update statement, you need to make sure that you don't miss any instance. 

Beside synchronous transactions listeners for transaction verification (see below), this is a second way to ensure data integrity. It shares the same risk of slowing down store operations due to costly computations. 

### Optimistic locking

Optimistic locking is the built-in mechanism for update operations. You have to refer to an existing instance `tx.update(anInstance)` to specify property updates. When the transaction is executed it is checked, if the referenced instance is still the current one or if it has been replaced in the meantime by another transaction.

With delete operations you have the choice between optimistic locking `delete(anInstance)` and no locking at all `delete("todo", 4)`.


### Locked Store
If multiple concurrend threads write transactions, the data may look different when a transaction is executed than it did when it transaction was created. To handle this there is the option of optimistic locking. But there might be situations where that isn't enough. It is therefore possible to lock the store and then define transactions that are immediately executed based on the current state of the store at that time. 

```java
	store.locked(ctx -> {
		var tx = ctx.transaction();
		ctx.snapshot().entity(Task.class).stream().forEach( task ->
			if (task.getDeadlineDate().isAfter(now())) {
				var task = tx.update(task);
				task.setDeadlineReached(true);
			}
		);
		tx.execute();
	});
```

### Transaction Listener

The bean store is pretty talkative. You can track all transactions. Depending on the purpose, there are different methods: The verifyX methods are used to register callbacks that check the validity of the transactions - and reject them if necessary. 

Transactions can be verified by callback code to enforce constraints. The beans can also define constraints by using the Java Bean Validation annotations (provided by a plugin).

Other callback options allow listeners to be informed on any change to the store, synchronously or asynchronously to the transaction execution. These callbacks can be used to create CQRS style *Read Models*.

All transactions are applied sequentially to the store. Synchronous listeners will receive the change events when an transaction is applied and before the execution code returns. Only verification listeners can abort a transaction by throwing an exception. Exceptions from other listener types will only be logged. Asynchronous listeners will receive the change events afterwards but also always in the order of their execution.


## Query store

The advantages of the concept come into play when querying the data: By using Java streams, even complex queries can be implemented very easily. 



## Migration

At the startup process, when the transactions are loaded initially from the file, the BeanStore factory does not use the Java Bean Classes to store the data. Instead it stores all data in maps. Only at the end of the startup process the maps are replaced by Java Bean instances. But just before that happens, the loaded data can be transformed through migration transactions. 

Use the `addMigration` method to register migrations. Each migration need to have a unique name. Make sure to always add migration transactions in the same order and with the same name. 

The BeanStore stores information about the every migration applied in the persistent file. Next time the data is loaded, only the migrations which were not yet applied are executed. New empty stores also save information about the last specified migration at the time of store creation. Subsequent startups will use this information to determine the migration to start from.

You can think of the migration name as a kind of database version.

```java

// renaming 'color' property into 'backgroundColor' property
builder.addMigration("rename-color-property", mtx -> {
	mtx.snapshot().mapEntity("picture").stream()
		.forEach(p -> {
			var update = mtx.update(p);
			update.put("backgroundColor", e.getString("color"));
		});	
});
```


## Plugins



### Bean Validation Plugin

[Jakarta Bean Validation](https://beanvalidation.org/) is a Java specification which lets you express constraints on object models via annotations. Register plugin `BeanValidationPlugin` to use this feature.

```java
class Employee extends AbstractEntity {
	@Min(value = 18, message = "Age should not be less than 18")
	private Integer age;
}

// configure Java Bean Validation
BeanValidationPlugin validationPlugin = new BeanValidationPlugin();
factory.addPlugin(validationPlugin);

var employee = tx.create(Employee.class);
employee.setAge(20);
tx.execute(); // throws ValidationException
	
```


#### Fulltext Search Plugin

The class `BeanStoreSearchPlugin` adds full text search capability to the BeanStore lib.

```java
// configure full text search
BeanStoreSearchPlugin searchPlugin = new BeanStoreSearchPlugin();
factory.addPlugin(searchPlugin);

// configure projection from instance to text to be indexed
searchPlugin.register(todoEntity, todo -> {
	return todo.getText();
});

var searchResult = searchPlugin.search("World");	
	
```


#### Transaction History Plugin

Use `BeanStoreHistoryPlugin` if you need to access a full change history of each instance. The simplistic implementation might consume too many resources in case of larger stores. Use it as a starting point of your refined and optimized solution.


#### Transaction Log Plugin

The `BeanStoreTransactionLogPlugin` lets you view all transactions, the transactions initially read as well as all transactions written to the file. `BeanStoreTransactionLogPlugin` listens to the *read* and *write* operations and logs them to a SLF4J Logger.


## Plugin API

The BeanStore plugin interface `BeanStorePlugin` contains a set of various callback methods. Implement this interface to provide re-usable components. The lib itself has some sample implementations that should give you an idea.



## Kryo Configuration Framework Support

The annotation `KryoConfig` 

### Close store

Calling `BeanStore.close` closes the transaction queue, i.e. no new transactions are accepted. Then all transactions currently in the queue are processed. Finally, the transaction writer is closed. `BeanStore.close` is a blocking operation, a call will only return when everything is done.



> [!NOTE]  
> The callback code is enqeued in the normal transaction queue.

## Ways to shoot in your foot

Beanstore can not hinder you to shoot yourself in your foot if you want to. These are the things you want to avoid:

* changeing serialization logic in a way which is not backward compatible
* setting values directy on stored instances
* messing up migrations
  

## Appendix

### Standard Data Types

All types listed in this section are already _kryo-serializable_, i.e. they can be used without the need to register a Kryo Serializer for them.

#### _java.lang.__
* String
* Integer
* Long
* Short
* Float
* Double
* Boolean
* Byte
* Character


#### _java.math.__
* BigInteger
* BigDecimal
* RoundingMode
		
#### _java.util.__
* Currency
* Locale
* Date

#### _java.net.__
* URL
* URI
		
#### _java.time.__
* Instant
* Duration
* LocalDateTime
* LocalDate
* LocalTime
* ZoneOffset
* ZoneId
* OffsetTime
* OffsetDateTime
* ZonedDateTime
* Year
* YearMonth
* MonthDay
* Period
* DayOfWeek
* Month

#### Arrays  
* byte[]
* char[]
* short[]
* int[]
* long[]
* float[]
* double[]
* boolean[]
* String[]


> [!CAUTION]  
> All but the arrays are immutable classes. If you use arrays as property values, you must be careful not to modify them once they are values of stored instances.

