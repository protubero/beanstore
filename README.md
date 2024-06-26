![](beanstore.svg)  __Beanstore__ is a fast and versatile data store for Java with a low barrier to entry and an easy-to-use API. The goal of the project is to offer a serious alternative to conventional database systems for certain usage scenarios. Therefore it offers features such as transactions, data validation and migration. The project is always useful when data needs to be stored permanently and a database seems too heavy and inflexible. The natural limitations are that all data must fit into memory. Also, with very frequent changes, startup time could possibly become a factor.

Beanstore has a rich callback API, e.g. you can reject a transaction in callback code. This allows implementing custom validation logic. And the API makes it easy to create projections and aggregations of the data and keep them up to date.

The way data is maintained and stored means that every state of the data from the past can be reproduced. Beanstore includes two ways to take advantage of this. On the one hand, you can access the complete history of a single instance. On the other hand, you can create and evaluate a snapshot of any historical state.

Beanstore has a plugin API that allows third parties to offer additional data-related add-ons, e.g. for data validation, search engines, etc. Beanstore comes with a Lucene-based in-memory search engine and a validation plugin based on Java Bean Validation.



## Table of contents

- [Versioning](#versioning)
- [Recent Releases](#recent-releases)
- [Maven Dependency](#maven-dependency)
- [Building from source](#building-from-source)
- [Quickstart](#quickstart)
- [Basic mechanism](#basic-mechanism)
- [Entities](#entities)
- [Build a store](#build-a-store)
- [Kryo Configuration](#kryo-configuration)
- [Transactions](#transactions)
- [Transaction Listener](#transaction-listener)
- [Query Store](#query-store)
- [Migration](#migration)
- [Historical States](#historical-states)
- [Plugin API](#plugin-api)
- [Plugins](#plugins)
  * [Bean Validation Plugin](#bean-validation-plugin)
  * [Fulltext Search Plugin](#fulltext-search-plugin)
  * [Instance History Plugin](#instance-history-plugin)
  * [Transaction Log Plugin](#transaction-log-plugin)
- [Close Store](#close-store)
- [HOWTO shoot yourself in the foot](#howto-shoot-yourself-in-the-foot)
- [Standard Data Types](#standard-data-types)
  

## Versioning

| Version   | Description |
| ------------- | ------------- |
| 0.8.x  | To be used for testing the library and possibly for small, time-limited personal projects. Undocumented breaking changes can occur in any release, no migration will be provided  |
| 0.9.x  | The library is considered mature enough to be used for small private projects. Breaking changes should be rare and well documented. Migrations, e.g. for changes of the persistence file format, will be provided.  |
| 1.x.x  | For all kind of projects. Semantic Versioning will be used. Migrations, e.g. for changes of the persistence file format, will be provided. Plugins with optional external dependencies like the Fulltext Search Plugin will be moved to a separate project |


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

> [!NOTE]
> see also https://github.com/protubero/beanstore-demo on how the lib can be used with a Spring Boot app.

## Basic mechanism

Other libraries try to apply persistence to ordinary Java objects. Bean Store takes a different approach. The instances in the bean store are completely under the control of the library. Instances in the store cannot be changed by setting properties. Changes must be described as transactions and left to the store to apply. Instances are immutable. Any changes made by a transaction result in a copy being created. Each execution of a transaction creates a new, immutable state of the data (snapshot).

Beanstore transactions describe changes to a set of “Java Beans”. These changes are simultaneously applied to the beans and stored in a log-structured file. The next time the application is started, all changes will be replayed to restore the last store state. If you've ever heard of event sourcing, you're already familiar with the concept. 

## Entities

Each store is a set of entities that resemble tables in a relational database. There are two different types of entities: 

- Entities which are represented by a Java Bean class
- Entities which are represented as Maps

At the persistence level, instances are nothing more than sets of key/value pairs. Thus it is possible to decide at the time of loading the data for each type whether it will be represented as maps or as 'Java Beans'. In the latter case, the bean classes define some kind of schema for the data.

The Beanstore bean spec:

- Must have a no-arg constructor
- Must inherit from `AbstractEntity`
- All values must be serializable with Kryo
- Must not have default values (i.e. no native types allowed)
- Must have Getters and Setters as required by the Java Bean Spec
- Must have a unique alias determined by the  _Entity_ annotation

We will refer to the 'Beanstore beans' as _Data Beans_ throughout this documentation. 

The `AbstractEntity` class implements a *Map* interface to make the bean properties accessible in a *map-ish* way. The methods of the map interface are only partly supported, unsupported operations are: *containsValue*, *remove*, *clean*, *values*.

All entities share the following properties:

- Each instance has a unique _id_ (long), which is assigned by the store itself
- Each instance has a _versionId_ (int) that is incremented with every change

  
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



- The BeanStore is designed as a store of immutable objects! _Stored beans_ will throw an exception if you call a setter method. 
- Do not declare your bean classes *final*. BeanStores uses ByteBuddy to dynamically creates subclasses of your beans. Which will not work with final classes.
- All values ​​should be instances of immutable classes. If the value's class does not guarantee immutability, you must still use it as if it were immutable. To be more specific: Never do `instanceX.getValueY().setPropertyZ(...)`, instead always set newly constructed values `instanceX.setValueY(newValueObj)`




## Build a store

The creation of a store follows the builder pattern. The configuration of the store builder determines 

 * how the data is __persisted__. Currently the only way to do so is to let Kryo serialize the data and append it to a file.
 * which __entities__ are in the store and what __type__ they are. Is the entity a map type or is it represented by a java class?
 * how a new store is __initialized__, i.e. which data is automatically created when a store is started for the first time.

Some of the advanced features have their own section in the documentation:
- [Migration](#migration)
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

Another way to specify the id and serializer is with the `KryoConfig` annotation. A class with this annotation can simply be registered with the `KryoConfiguration.register(Class clazz)` method.


Beanstore comes with one implementation of the Kryo Serializer interface to simplify the serialization of your own value classes. The __PropertyBeanSerializer__ class serializes all _declared fields_ of a class. It treats the field of an instance as key/value pairs. It offers two mechanism to support the evolution of the class:

- Use the _KryoAlias_ Annotation to rename fields, i.e. set the old name as an alias so that the deserialization process can map persisted values with the old name to the field with the new name
- If the class implements _SetPropertyValue_ it can handle the setting of the values all by itself
- Implementing _AfterDeserialization_ the class will receive an event when all field values were set. This could be used to shift or convert values. 




> [!NOTE]  
> Kryo IDs have to be greater than 200!



## Transactions

All changes to the store are executed as transactions. A transaction can be thought of as a list of instructions:

- Create a new 'ToDo' instance with properties [('text', 'buy stuff')]
- Update properties of an existung 'Employee' instance with id 35 [('age', 34), ('firstName', 'Kurt')]
- Delete 'ToDo' instance with id 44
- ...

Transactions are created in code like this:

```java
// create new transaction
ExecutableBeanStoreTransaction tx = store.transaction();

var newTodo = tx.create("ToDo");
newTodo.put("text"), "buy stuff";

// the updEmployee object's purpose is to record the values being set!
var updEmployee = tx.update(Keys.key(Employee.class, 35));
updEmployee.setAge(35);
updEmployee.setFirstName("Kurt");

tx.delete(Keys.key("ToDo", 44));

// execute transaction
tx.execute();

```

What happens when the `execute()` method is invoked? The store has a transaction queue (FIFO) into which every new transaction is queued. The storage accepts and processes one transaction at a time, creating a new state of the data with each one. Every transaction increments the __version__ id of the state by one. Each transaction ultimately receives an ID that is identical to the __version__ of the store it created.

Transaction execution is inherently asynchronous, but often you prefer to wait for the transaction to complete. The Bean Store API provides methods for blocking and non-blocking execution as well:


```java
	// Need to inspect the result to see if the transaction was successful
	CompletableFuture<BeanStoreTransactionResult> executeAsync();

	// Will throw an exception if the transaction fails
	BeanStoreTransactionResult execute() throws TransactionFailure {...}
```

The transaction will be written to the file immediatly, just before the store data is changed in memory. 

__Optimistic locking__

You can choose for every single delete and update operation of the transaction if it should fail if the target instance has changed in the meantime. Simply use `Keys.versionKey()` instead of `Keys.key()` to target a specific version of the instance.


__Pessimistic locking__

When multiple concurrent threads write transactions, the data may look different when a transaction is executed than when the transaction was created. To deal with this, there is the option of optimistic locking described above. But there may be situations where that is not enough. It is therefore possible to lock the memory and then define transactions that will be executed immediately based on the current state of the memory at that time.

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

The bean store is pretty talkative. You can track all transactions. Depending on the purpose, there are different methods: 

- The `verifyX` methods are used to register callbacks that check the validity of the transactions - and reject them if necessary. Only verification listeners can abort a transaction by throwing an exception. Exceptions from other listener types will only be logged.
- The `onChangeX` methods allow listeners to be informed on any change to the store, right after the changes were applied to the store objects. These callbacks can be used to create CQRS style *Read Models* that are always up to date.
- Callback code registered with the `onChangeXAsync` methods is called asynchronously, but also always in the order of their execution. This type is best suited when you do not want the execution of the callback code to slow down the execution time of the transaction.

Example code might look like this:

```java

store.callbacks().verifyInstance(Employee.class, evt -> {
	if (evt.newInstance().getAge() < 18) {
		throw new RuntimeException("Age must be greater than 18");
	}
});

// keep track of todo count by priority
store.callbacks().onChangeInstance("ToDo", evt -> {
	switch(evt.type()) {
	case Create:
		incPrio((Priority) evt.newInstance().get("priority"));
		break;
	case Update:
		if (evt.replacedInstance().getPriority() != evt.newInstance().getPriority()) {
			decPrio((Priority) evt.replacedInstance().get("priority"));
			incPrio((Priority) evt.newInstance().get("priority"));
		}
		break;
	case Delete:
		decPrio((Priority) evt.replacedInstance().get("priority"));
		break;
	}
});

```

## Query store

The advantages of the concept come into play when querying the data: By using Java streams, even complex queries can be implemented very easily. All queries are executed on a snapshot. The latest snapshot from a store can be retrieved by calling the `snapshot()` method of the bean store. A snapshot offers multiple ways to get an entity store, e.g. by the entity alias or by the bean class. A snapshot has a unique __version__ number that corresponds to the ID of the transaction that resulted in that state.

```java
	// iterating all instances of an entity is simple
	var allEmployees = store.snapshot().entity(Employee.class).stream().collect(Collectors.toList());
	allEmployees.forEach(System.out::println);

	// find an instance by alias/class and id
	Employee employee23 = snapshot.find(PersistentObjectKey.of(Employee.class, 23));

```


What else can you do with a snapshot?

```java
	// retrieving a snapshot
	BeanStoreSnapshot snapshot = store.snapshot();
	int version = snapshot.version();

	// meta data is also accessible on a snaphsot
	BeanStoreMetaInfo metaInfo = snapshot.meta();

	// map-based entity store
	EntityStoreSnapshot<MapObject> todoStore = snapshot.mapEntity("todo");

	// bean based entity store
	EntityStoreSnapshot<Employee> employeeStore = snapshot.entity(Employee.class):
		
```

The `EntityStoreSnapshot` class has a lot of useful methods to iterate over the instances and to find them:

```java
	// Meta information about the entity.
	BeanStoreEntity<T> meta();

	// Find an instance by id. Returns null if no object with that id exists.
	T find(long id);
	
	// Find an instance by key (alias & id).
	T find(PersistentObjectKey<?> key) {...}
	
	// Stream all instances
	Stream<T> stream();

	 // Iterate over all entity instances
	Iterator<T> iterator() {...}

	// Returns the number of stored instances.
	int count();

	 // Returns an unmodifieable list of all stored instances
	List<T> asList() {...}
```



> [!NOTE]
> Use the `mapEntity` method to get an entity store if you know that an entity alias refers to a map-based entity store. 

## Historical States

With another builder you can reproduce any historical states of the existing store. `MapStoreSnapshotBuilder` is initialized in the same way as the normal builder. The `states` method provides information about all historical states of the store. Use the `build(int state)` method to create a `BeanStoreSnapshot` of one state. Unlike the normal build process, the snapshot only consists of map-like entities. This has to be the case because it cannot be guaranteed that any intermediate state can be mapped to the current beans.

Since the file is only opened for reading, you can use the builder even if the normal store is writing new transactions at the same time.

```java
KryoConfiguration kryoConfig = KryoConfiguration.create();
KryoPersistence persistence = KryoPersistence.of(new File(someDir, "file.bst"), kryoConfig);
MapStoreSnapshotBuilder builder = MapStoreSnapshotBuilder.init(persistence);

BeanStoreSnapshot snaposhot = builder.build(5);

```


## Migration

At the startup process, when the transactions are loaded initially from the file, the BeanStore factory does not use the Java Bean Classes to store the data. Instead it stores all data in maps. Only at the end of the startup process the maps are replaced by Java Bean instances. But just before that happens, the loaded data can be transformed through migration transactions. 

Use the `addMigration` method to register migrations. Each migration need to have a unique name. Make sure to always add migration transactions in the same order and with the same name. 

Every migration code creates one migration transaction. The name of the migration is persisted together with that transaction. Thus the BeanSTore knows at load time which tranasctions have already been applied to the data. Next time the data is loaded, only the migrations which were not yet applied are executed. New empty stores also save information about the last specified migration at the time of store creation. Subsequent startups will use this information to determine the migration to start from.

```java

// renaming 'color' property into 'backgroundColor' property
builder.addMigration("rename-color-property", mtx -> {
	// Unlike normal transactions, the migration transaction also allows access to the data
	mtx.snapshot().mapEntity("picture").stream()
		.forEach(p -> {
			var update = mtx.update(p);
			update.put("backgroundColor", e.getString("color"));
		});	
});
```

> [!NOTE]
> You can think of the migration name as a kind of database version.

## Plugin API

The BeanStore plugin interface `BeanStorePlugin` contains a set of various callback methods. Implement this interface to provide re-usable components. The lib itself has some sample implementations that should give you an idea.



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
employee.setAge(16);
tx.execute(); // throws ValidationException
	
```


### Fulltext Search Plugin

The `BeanStoreSearchPlugin` provides customizable full text search capability. You can determine for each entity whether it should be indexed and which text should be indexed. For each entity, an individual mapping of an instance onto a text to be indexed is determined.

The index is build at load time and is kept in memory. That's why you can easily change the logic to quickly find the best settings. 

```java
// configure full text search
BeanStoreSearchPlugin searchPlugin = new BeanStoreSearchPlugin();
factory.addPlugin(searchPlugin);

// configure projection from instance to text to be indexed
searchPlugin.register(todoEntity, todo -> {
	return todo.getText();
});

List<AbstractPersistentObject> searchResult = searchPlugin.search("World");
	
```


### Instance History Plugin

Use `BeanStoreHistoryPlugin` if you need to access a full change history of instances. First you have to register all entities for which you want to have the instance history. Then you can class `changes(PersistentObjectKey key)` at any time to get a full and up-to-date history of the instance.


### Transaction Log Plugin

The `BeanStoreTransactionLogPlugin` lets you view all transactions, the transactions initially read as well as all transactions written to the file. `BeanStoreTransactionLogPlugin` listens to the *read* and *write* operations and logs them to a SLF4J Logger.

## Close store

Calling `BeanStore.close` closes the transaction queue, i.e. no new transactions are accepted. Then all transactions currently in the queue are processed. Finally, the transaction writer is closed. `BeanStore.close` is a blocking operation, a call will only return when everything is done.


> [!NOTE]  
> The callback code is enqeued in the normal transaction queue.

## HOWTO shoot yourself in the foot

Beanstore can not hinder you to shoot yourself in the foot. These are the things you want to avoid:

* Changing serialization logic in a way which is not backward compatible
* Ignoring the immutability of stored instances and setting values directly on stored instances
* Messing up migrations
  

## Standard Data Types

All types listed in this section are already _kryo-serializable_, i.e. they can be used without the need to register a Kryo Serializer for them.

__java.lang.___
* String
* Integer
* Long
* Short
* Float
* Double
* Boolean
* Byte
* Character


__java.math.___
* BigInteger
* BigDecimal
* RoundingMode
		
__java.util.___
* Currency
* Locale
* Date

__java.net.___
* URL
* URI
		
__java.time.___
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

__Arrays__
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

