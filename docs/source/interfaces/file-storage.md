# File Storage

The file-storage interface provides functionality for storing referenced files (e.g. asset thumbnail, SubmodelElement File).

Example of a file-storage configuration:
```json
{
	"filestorage" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.filestorage.memory.FileStorageInMemory"
	}
}
```

## In-Memory

The in-memory-based filestorage keeps all files stored in-memory.

### Configuration Parameters

In-memory-based FileStorage does not support any configuration parameters.

#### Example

Example configuration for the in-memory-based FileStorage:

```json
{
	"filestorage" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.filestorage.filesystem.FileStorageInMemory"
	}
}
```

## File System

The file system-based FileStorage keeps all files stored in the file system of the local machine.
Any change request, such as changing a file, results in a change in the file system.
Thus, changes are stored permanently.

### Configuration Parameters

| Name | Allowed Value | Description |
|:--| -- | -- |
| path | String |  _optional_ The path/directory to use to store the files, default: . |
| existingDataPath | String |  _optional_ A path/directory containing data that should be available on start-up. This data will never be modified or deleted. , default: null |

#### Example

Example configuration for the Filesystem-based FileStorage:

```json
{
	"filestorage" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.filestorage.filesystem.FileStorageFileSystem",
		"path": "./my/file/cache",
		"existingDataPath": "./my/initial/data"
	}
}