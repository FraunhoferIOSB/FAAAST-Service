# Persistence

Each persistence configuration supports at least the following configuration parameters:
-   `initialModel` (optional): `AASEnvironment` object containing the model to load initially. This option can only be set via code, not via configuration file.
-   `initialModelFile` (optional): Path to the model file to load intially

Example of a persistence configuration:
```json
{
	"persistence" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemory",
		"initialModel" : "{pathTo}/FAAAST-Service/misc/examples/demoAAS.json"
	}
}
```

## In-Memory

The In-Memory Persistence keeps the AAS environment model parsed at startup in the local memory. Any change request, such as changing the value of a property, results in a change to the AAS environment model in the local memory. If you do not want the model object to be changed from within the file persistence, make sure to create a deep copy of the model before passing it to the file persistence, e.g. by calling `DeepCopyHelper.deepCopy(...)`.


The In Memory Persistence has no additional configuration parameters.

Not yet implemented:
-   AASX Packages
-   Package Descriptors
-   SubmodelElementStructs

## File-based

The file-based persistence keeps the entire AAS Environment in a model file which is stored at the local machine. Any change request, such as changing the value of a property, results in a change to the AAS environment model file. Thus, changes are stored permanently.

File Persistence configuration supports the following configuration parameters:
-   `dataDir` (optional, default: `/`): Path under which the model file created by the persistence is to be saved
-   `keepInitial` (optional, default: `true`): If false the model file parsed on startup will be overridden with changes. If true a copy of the model file will be created by the persistence which keeps the changes.
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