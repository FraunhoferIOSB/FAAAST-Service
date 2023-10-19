# Filesystem-based Filestorage
The file-based filestorage keeps all files stored in a file-system at the local machine. Any change request, such as changing a file, results in a change in the file-system. Thus, changes are stored permanently.

Example configuration for the Filesystem-based filestorage:

```json
{
	"filestorage" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.filestorage.filesystem.FileStorageFileSystem"
	}
}
```
