# Persistence

The Persistence interface is responsible for storing the AAS model.

Each Persistence configuration supports at least the following configuration properties:

:::{table} Common configuration properties of for all Persistence implementations.
| Name                             | Allowed Value | Description                                                                                                                                                                                               | Default Value                       |
| ---------------------------------| ------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------- |
| initialModel<br>*(optional)*     | String        | An `Environment` object containing the model to load initially.<br>This can only be set when used via code, not via configuration file.<br>This has precedence over `initialModelFile` when both are set. |                                     |
| initialModelFile<br>*(optional)* | String        | Path to a model file to load initially.                                                                                                                                                                   |                                     |
:::

## Transactions

Some operations need several persistence calls to take effect together. For example, replacing a Submodel whose id changed rewrites every Asset Administration Shell referencing it, deletes the old Submodel and saves the new one. If
a part fails, shells are left referencing a Submodel that does not exist.

Every persistence operation comes in two forms: one taking a `Transaction` as its last parameter, and a convenience
overload without it. The overload without a transaction (equivalently, passing `null`) runs the operation on its own and
it takes effect immediately; passing a transaction groups it with all other operations using the same transaction.
Existing code that does not care about transactions therefore keeps working unchanged. Implementations only implement
the transaction-taking form - the convenience overloads are inherited defaults.

`Persistence.inTransaction(work)` (and its void variant `runInTransaction`) executes such a sequence as one unit: it
starts a transaction, hands it to the unit of work, and commits it - or rolls it back if the unit of work fails.

```java
Submodel oldSubmodel = persistence.inTransaction(tx -> {
    Submodel existing = persistence.getSubmodel(submodelId, QueryModifier.DEFAULT, tx);
    persistence.deleteSubmodel(submodelId, tx);
    persistence.save(newSubmodel, tx);
    return existing;
});
```

Both methods have a two-argument form taking a transaction to join, e.g. `inTransaction(tx, work)`. If that transaction
is non-null the unit of work joins it and the outer caller stays responsible for committing, rolling back and closing
it; if it is null a new transaction is started as above. This lets a helper participate in a caller's transaction
without knowing whether there is one.

Because `Consumer` and `Function` cannot throw checked exceptions, a unit of work that needs to do so must manage the
transaction itself via `beginTransaction()`. Leaving the block without committing rolls the transaction back:

```java
try (Transaction tx = persistence.beginTransaction()) {
    // ... work that may throw checked exceptions ...
    tx.commit();
}
```

**The atomicity guarantee is optional and depends on the implementation:**

| Implementation | Transactions |
| -------------- | ------------ |
| In-Memory      | ✗            |
| File-based     | ✗            |
| Mongo          | ✗            |
| Postgres       | ✓            |

Implementations that do not support transactions hand out a no-op transaction: all operations take effect immediately
and rolling back does *not* undo anything, so whatever was written before a failure stays written.

Note that all exceptions defined by FA³ST Service are unchecked (they extend `RuntimeException`). The `throws` clauses
on the persistence API are kept for documentation, but the compiler no longer forces callers to handle them.

When writing request handlers, keep message bus publishing, file storage access and asset connection calls
**outside** the transaction action. Emitting an event for a change that is later rolled back cannot be undone.

Note that no transaction can span both the persistence and the file storage. The handlers dealing with `File` elements
and thumbnails therefore order their two writes so that a failure in between leaves an unreferenced blob.

## In-Memory

The In-Memory Persistence keeps the AAS model in the local memory.
This means, that once FA³ST Service is stopped or crashes, all changes made during runtime are lost.

:::{important}
If you use In-Memory Persistence from code by setting the `initialModel` property, the passed instance of `Environment` will be modified directly (as always the case in Java with pass-by-reference).
If you do not want the original instance to be modified by FA³ST Serivce, call `DeepCopyHelper.deepCopy(...)` with the `Environment` to create a copy before passing it to FA³ST.
:::

### Configuration

In-Memory Persistence has no additional configuration properties.

```{code-block} json
:caption: Example configuration for In-Memory Persistence.
:lineno-start: 1
{
	"persistence" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemory",
		"initialModel" : "{pathTo}/FAAAST-Service/misc/examples/model.json"
	},
	//...
}
```


## File-based

The File-based Persistence stores the AAS model in a file according to the AAS specification.
Therefore, changes are stored permanently even when FA³ST Service is stopped or crashes.

:::{important}
Each modification of the model results in writing the whole model to the file which might become a performance issue for larger models.
:::

### Configuration

:::{table} Configuration properties of File-based Persistence.
| Name                        | Allowed Value       | Description                                                                                                                                                                                                     | Default Value              |
| ----------------------------| ------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------- |
| dataDir<br>*(optional)*     | String              | Path where the model file created by the persistence should be saved.                                                                                                                                           | .                          |
| dataformat<br>*(optional)*  | AASX<br>JSON<br>XML | Data format to use when storing.<br>Ignored when `keepInitial` is set to `true`.                                                                                                                                | same as `initialModelFile` |
| keepInitial<br>*(optional)* | Boolean             | If true, `initialModelFile` will not be modified but instead a copy will be created in `dataDir` where the changes will be saved.<br> If false, all changes will be written directly to the `initialModelFile`. | true                       |
:::

```{code-block} json
:caption: Example configuration for File-based Persistence.
:lineno-start: 1
{
	"persistence" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.persistence.file.PersistenceFile",
		"initialModelFile" : "{pathTo}/FAAAST-Service/misc/examples/model.json",
		"dataDir": ".",
		"keepInitial": true,
		"dataformat": "XML"
	},
	//...
}
```


## Mongo (deprecated)

:::{warning}
**MongoDB persistence is deprecated and will be remove in v2.0.**
:::

The Mongo Persistence stores the AAS model in a MongoDB according to the AAS specification.
Therefore, changes are stored permanently even when FA³ST Service is stopped or crashes.


### Configuration

:::{table} Configuration properties of MongoDB-based Persistence.
| Name                     | Allowed Value       | Description                                                                                            | Default Value |
| -------------------------| ------------------- | ------------------------------------------------------------------------------------------------------ | ------------- |
| connectionString<br>     | String              | The connection string where the MongoDB is located.                                                    |               |
| database<br>*(optional)* | String              | The name of the database to be used inside the MongoDB.                                                | `faaast`      |
| override<br>*(optional)* | Boolean             | If true, FA³ST persistence will always override the previous database, this might result in data loss. | false         |

:::

```{code-block} json
:caption: Example configuration for MongoDB-based Persistence.
:lineno-start: 1
{
	"persistence" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.persistence.mongo.PersistenceMongo",
		"connectionString" : "mongodb://localhost:27017",
		"database": "faaast-database",
		"override": true
	},
	//...
}
```

## Postgres

The Postgres Persistence stores the AAS model in a Postgres DB according to the AAS specification.
Therefore, changes are stored permanently even when FA³ST Service is stopped or crashes.

:::{important}
Each modification of the model results in only writing the specific part to the Postgres table which should improve performance
:::

### Configuration

:::{table} Configuration properties of Postgres-based Persistence.
| Name                     | Allowed Value       | Description                                                                                            | Default Value |
| -------------------------| ------------------- | ------------------------------------------------------------------------------------------------------ | ------------- |
| jdbcUrl<br>              | String              | The connection string where the PostgresDB is located.                                                 |               |
| username<br>             | String              | The username to connect to Postgres.                                                                   |               |
| password<br>             | String              | The password to connect to Postgres.                                                                   |               |
| override<br>*(optional)* | Boolean             | If true, FA³ST persistence will always override the previous database, this might result in data loss. | false         |
| maximumPoolSize<br>*(optional)* | Integer      | Maximum number of pooled database connections. A transaction holds exactly one connection for its whole duration, so this also caps the number of concurrent transactions. | 10 |

:::

```{code-block} json
:caption: Example configuration for Postgres-based Persistence.
:lineno-start: 1
{
	"persistence" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.persistence.postgres.PersistencePostgres",
		"jdbcUrl" : "jdbc:postgresql://localhost:5432/faaast",
		"username": "faaast",
		"password": "faaast",
		"override": true
	},
	//...
}
```
