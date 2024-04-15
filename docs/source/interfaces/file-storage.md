# FileStorage

The FileStorage interface is responsible for managing auxiliary files like thumbnails or files referenced by the AAS model.

## In-Memory

The In-Memory FileStorage keeps all files stored in memory.
This means, that once FA³ST Service is stopped or crashes, all changes made during runtime are lost.

### Configuration

In-Memory FileStorage does not support any configuration parameters.


### Example

```{code-block} json
:caption: Example configuration for In-Memory FileStorage.
:lineno-start: 1
{
	"fileStorage" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.filestorage.memory.FileStorageInMemory"
	},
	//...
}
```

## FileSystem

The FileSystem-based FileStorage keeps all files stored in the file system of the local machine.
Any change request, such as changing a file, results in a change in the file system.
Thus, changes are stored permanently.

### Configuration

:::{table} Configuration properties of FileSystem FileStorage.
| Name                             | Allowed Value | Description                                                                                                            | Default Value              |
| -------------------------------- | ------------- | ---------------------------------------------------------------------------------------------------------------------- | -------------------------- |
| existingDataPath<br>*(optional)* | String        | A path/directory containing data that should be available on start-up.<br>This data will never be modified or deleted. |                            |
| path<br>*(optional)*             | String        | The path/directory to use for storing the files.                                                                       | .                          |
:::

#### Example

```{code-block} json
:caption: Example configuration for FileSystem FileStorage.
:lineno-start: 1
{
	"fileStorage" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.filestorage.filesystem.FileStorageFilesystem",
		"path": "./my/file/cache",
		"existingDataPath": "./my/initial/data"
	},
	//...
}
```
