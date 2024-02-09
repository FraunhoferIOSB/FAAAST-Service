# File System-based FileStorage

The file system-based FileStorage keeps all files stored in the file system of the local machine.
Any change request, such as changing a file, results in a change in the file system.
Thus, changes are stored permanently.

## Configuration Parameters

| Name | Allowed Value | Description |
|:--| -- | -- |
| path | String |  _optional_ The path/directory to use to store the files, default: . |
| existingDataPath | String |  _optional_ A path/directory containing data that should be available on start-up. This data will never be modified or deleted. , default: null |

### Example

Example configuration for the Filesystem-based FileStorage:

```json
{
	"filestorage" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.filestorage.filesystem.FileStorageFileSystem",
		"path": "./my/file/cache",
		"existingDataPath": "./my/initial/data"
	}
}
```
