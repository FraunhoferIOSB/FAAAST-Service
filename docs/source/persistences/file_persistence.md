# File-based Persistence
The file-based persistence keeps the entire AAS Environment in a model file which is stored at the local machine. Any change request, such as changing the value of a property, results in a change to the AAS environment model file. Thus, changes are stored permanently.

File Persistence configuration supports the following configuration parameters:
-   `dataDir` (optional, default: `/`): Path under which the model file created by the persistence is to be saved
-   `keepInitial` (optional, default: `true`): If false the model file parsed on startup will be overriden with changes. If true a copy of the model file will be created by the persistence which keeps the changes.
-   `dataformat` (optional, default: same data format as input file): Determines the data format of the created file by file persistence. Ignored if the `keepInitial` parameter is set to false. Supported data formats are `JSON`, `XML`, `AML`, `RDF`, `AASX`, `JSONLD`, `UANODESET`.

Example configuration for the file persistence:

```json
{
	"persistence" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.persistence.file.PersistenceFile",
		"initialModelFile" : "{pathTo}/FAAAST-Service/misc/examples/demoAAS.json",
		"dataDir": ".",
		"keepInitial": true,
		"dataformat": "XML"
	}
}
```

Not yet implemented:
-   AASX Packages
-   Package Descriptors
-   SubmodelElementStructs