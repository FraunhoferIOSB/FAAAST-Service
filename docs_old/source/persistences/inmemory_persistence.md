### In-Memory Persistence
The In-Memory Persistence keeps the AAS environment model parsed at startup in the local memory. Any change request, such as changing the value of a property, results in a change to the AAS environment model in the local memory. If you do not want the model object to be changed from within the file persistence, make sure to create a deep copy of the model before passing it to the file persistence, e.g. by calling `DeepCopyHelper.deepCopy(...)`.


The In Memory Persistence has no additional configuration parameters.

Not yet implemented:
-   AASX Packages
-   Package Descriptors
-   SubmodelElementStructs