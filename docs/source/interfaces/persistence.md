# Persistence

The Persistence interface is responsible for storing the AAS model.

Each Persistence configuration supports at least the following configuration properties:

:::{table} Common configuration properties of for all Persistence implementations.
| Name                             | Allowed Value | Description                                                                                                                                                                                               | Default Value                       |
| ---------------------------------| ------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------- |
| initialModel<br>*(optional)*     | String        | An `Environment` object containing the model to load initially.<br>This can only be set when used via code, not via configuration file.<br>This has precedence over `initialModelFile` when both are set. |                                     |
| initialModelFile<br>*(optional)* | String        | Path to a model file to load initially.                                                                                                                                                                   |                                     |
:::

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


## Mongo

The Mongo Persistence stores the AAS model in a MongoDB according to the AAS specification.
Therefore, changes are stored permanently even when FA³ST Service is stopped or crashes.

:::{important}
Each modification of the model results in only writing the specific part to the MongoDB document which should improve performance
:::

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
