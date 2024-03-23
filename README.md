

Beanstore is a fast and versatile data store for Java with a low barrier to entry and an easy-to-use API. The goal of the project is to offer a serious alternative to conventional database systems for certain usage scenarios. Therefore it offers features such as transactions, data validation, and migration. The project is always useful when data needs to be stored permanently and a database seems too heavy and inflexible. The natural limitations are that all data must fit into memory. With very frequent changes, startup time could possibly become a factor.

Beanstore transactions describe changes to a set of “Java Beans”. These changes are simultaneously applied to the beans and stored in a log-structured file. The next time the application is started, all changes will be replayed to restore the last store state. If you've ever heard of event sourcing, you're already familiar with the concept. The Java Beans specification requires that beans be serializable through Java Object Serialization. Instead, we use Kryo as a serialization framework. And we require that the beans have no default values, which effectively prevents the use of native types like int and boolean. We will refer to them as “data beans” throughout this documentation. We won't use the POJO term because the classes still have to follow the remaining Java Bean Rules (getters/setters and no-arg constructor).

Each store is a set of entities that resemble tables in a relational database. You can choose whether such an entity is represented by a data bean, i.e. whether there is an associated Java class. If not, beanstore uses a generic map-like representation.
You can switch between the two representations at any time.

Beanstore has a rich callback API, e.g. you can reject a transaction in callback code. This allows implementing custom validation logic. And the API makes it easy to create projections and aggregations of the data and keep them up to date.

The way data is maintained and stored means that every state of the data from the past can be reproduced. Beanstore includes two ways to take advantage of this. On the one hand, you can access the complete history of a single instance. On the other hand, you can create and evaluate a snapshot of any historical state.

Beanstore has a plugin API that allows third parties to offer additional data-related add-ons, e.g. for data validation, search engines, etc. Beanstore comes with a Lucene-based in-memory search engine and a validation plugin based on Java Bean Validation.


Table of contents


# Versioning
	0.8.x For Testing and short time projects. No Rules, undocumented breaking changes anytime, any version, no migration provided
	0.9.x For hobby projects, breaking changes should be rare and well documented, migration will be provided
	from 1.0.0 Semantic Versioning, migration will be provided, all kind of projects

# Recent releases
	


# Installation
	With Maven
```xml
<dependency>
    <groupId>de.protubero</groupId>
    <artifactId>beanstore</artifactId>
    <version>0.8.5</version>
</dependency>
```
	
	
	Building from source

# Quickstart

```java

// 1. Create a Java Bean Class, inherit from AbstractEntity
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

// 2. Create and configure the BeanStore factory, register the bean class
BeanStoreFactory factory = BeanStoreFactory.of(new File("c:/your/path/app.kryo"));
factory.registerEntity(ToDo.class);

// 3. Create the BeanStore
var store = factory.create();

// 4. Create a new JavaBeans instance using a transaction
var tx = store.transaction();		
ToDo newToDo = tx.create(ToDo.class);
newToDo.setText("Hello World");
tx.execute();

// 5. read a list of all ToDos
var allToDos = store.read().entity(ToDo.class).stream().collect(Collectors.toList());
```


	(Sample App)

# Build a store

The creation of a store follows the builder pattern. The configuration of the store builder determines how the data is persisted, which entities are in the store and what type they are. You also determine how a new store is initialized and how data in an outdated schema is migrated.

Commented Example
 

## Builder Configuration: Persistency
## Builder Configuration: Entities
## Builder Configuration: New Store Initialization
## Builder Configuration: Migrations
## register plugins


# Transactions

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


## Optimistic locking
## locked Store


# Query store

The advantages of the concept come into play when querying the data: By using Java streams, even complex queries can be implemented very easily. 

Callbacks

The bean store is pretty talkative. You can track all transactions. Depending on the purpose, there are different methods: The verifyX methods are used to register callbacks that check the validity of the transactions - and reject them if necessary. 

# Migration

# Advanced Values / Kryo

# Querying Historic States


# Plugin

## Plugin API
## Default Plugins

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



### build on

We use [Kryo](https://github.com/EsotericSoftware/kryo) to serialize and deserialize transactions, [ByteBuddy](https://bytebuddy.net) to enhance bean classes at runtime and [PCollections](https://github.com/hrldcpr/pcollections) to facilitate concurrent reading and writing.

PCollections

ByteBuddy

lucene 

rxjava


# Entities

BeanStore data is stored as instances of [Java Beans](https://blog.joda.org/2014/11/the-javabeans-specification.html). 

A BeanStore entity class has to extend class `AbstractEntity`. It must have a no-arg constructor and expose its properties by setters and getters - as required by the Java Beans Spec. Deviating from the specification, it does not have to be serializable. 

Only the following property types are permitted:
* Integer
* Long
* Float
* Double
* Byte
* Short
* Character
* Boolean
* String
* Instant

The list only contains the wrapper classes of the Java primitive types. Of course all Java primitive types can be used as well.

Each entity is associated with a unique *alias*. Annotate each entity class with an *Entity* annotation to assign an unique alias. The *key* of a bean is always a long value, stored in the *id* field of *AbstractEntity*. IDs are automatically generated by BeanStore for newly created beans. The id is unique per entity. The *full* key of a BeanStore instance is a combination of alias and id.

Do not declare your bean classes *final*. BeanStores uses ByteBuddy to dynamically creates subclasses of your beans. Which will not work with final classes.

The `AbstractEntity` class implements a *Map* interface to make the bean properties accessible in a *map-ish* way. The methods of the map interface are only partly supported, unsupported operations are: *containsValue*, *remove*, *clean*, *values*.

The BeanStore is designed as a store of immutable objects! Stored beans will throw an exception if you call a setter method. You can easily shoot yourself in the foot by changing field values of stored beans. BeanStore only restricts access per setter method.




#### Register Entity Classes

All entity classes have to be registered with the BeanStore factory, using the `registerEntity` method.

#### Initialization of a new Store

When a store is created, the factory checks if the transaction log file is empty or not (i.e. if the file exists). If there are no transactions, the new empty store is initialized by the listener which is passed as an argument of the `initNewStore` method.

#### Migration Transactions

At the startup process, when the transactions are loaded initially from the file, the BeanStore factory does not use the Java Bean Classes to store the data. Instead it stores all data in maps. Only at the end of the startup process the maps are replaced by Java Bean instances. But just before that happens, the loaded data can be transformed through migration transactions. 

Use the `addMigration` method to register migrations. Each migration need to have a unique name. Make sure to always add migration transactions in the same order and with the same name. 

The BeanStore stores information about the last migration applied in the persistent file. Next time the data is loaded, only the migrations which were not yet applied are executed. New empty stores also save information about the last specified migration at the time of store creation. Subsequent startups will use this information to determine the migration to start from.

You can think of the migration name as a kind of database version.


### Write Data

#### Transactions



#### Optimistic Locking

Optimistic locking is the built-in mechanism for update operations. You have to refer to an existing instance `tx.update(anInstance)` to specify property updates. When the transaction is executed it is checked, if the referenced instance is still the current one or if it has been replaced in the meantime by another transaction.

With delete operations you have the choice between optimistic locking `delete(anInstance)` and no locking at all `delete("todo", 4)`.


#### Transaction Listeners

Transactions can be verified by callback code to enforce constraints. The beans can also define constraints by using the Java Bean Validation annotations (provided by a plugin).

Other callback options allow listeners to be informed on any change to the store, synchronously or asynchronously to the transaction execution. These callbacks can be used to create CQRS style *Read Models*.

All transactions are applied sequentially to the store. Synchronous listeners will receive the change events when an transaction is applied and before the execution code returns. Only verification listeners can abort a transaction by throwing an exception. Exceptions from other listener types will only be logged. Asynchronous listeners will receive the change events afterwards but also always in the order of their execution.


### Read Data

`BeanStore.read()` returns a `BeanStoreReadAccess` implementation that provides read access methods. 

The `BeanStoreReadAccess` interface has methods to

* get type information about the stored entities
* find single instances by type alias and id
* "query" entities with java streams
* return a snapshot version of the store (i.e. subsequent transactions will have no effect on the snapshot store.)

Calling `BeanStore.close` first closes the transaction queue for new entries and then closes the transaction writer. 


### Plugins

The BeanStore plugin interface `BeanStorePlugin` contains a set of various callback methods. Implement this interface to provide re-usable components. The lib itself has some sample implementations that should give you an idea.


#### Fulltext search

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



#### Transaction History

Use `BeanStoreHistoryPlugin` if you need to access a full change history of each instance. The simplistic implementation might consume too many resources in case of larger stores. Use it as a starting point of your refined and optimized solution.


#### Transaction Log

The `BeanStoreTransactionLogPlugin` lets you view all transactions, the transactions initially read as well as all transactions written to the file. `BeanStoreTransactionLogPlugin` listens to the *read* and *write* operations and logs them to a SLF4J Logger.

