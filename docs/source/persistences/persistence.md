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