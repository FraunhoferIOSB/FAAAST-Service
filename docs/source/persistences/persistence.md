# Persistence
Each persistence configuration supports at least the following configuration parameters:
-   `initialModel` (optional, can be overriden by CLI parameter or environment variable): Path to the AAS Environment model file
-   `decoupleEnvironment` (optional, default: `true`): Only applicable if the AAS Environment is given as Java Object. If set to true, the persistence makes a deep copy of the AAS Environment and decouples the internal AAS Environment from the AAS Environment parsed on startup. If set to false, the same object instance is used in the FA³ST Service, which may have unexpected side effects.

Example of a persistence configuration:
```json
{
	"persistence" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemory",
		"initialModel" : "{pathTo}/FAAAST-Service/misc/examples/demoAAS.json",
		"decoupleEnvironment" : true
	}
}
```