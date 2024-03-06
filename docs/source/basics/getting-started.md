# Getting Started

FA³ST Service uses the concept of an open architecture. This means, it is designed to be easily extenadable and customizable. 

The main components of FA³ST Service are `AAS Model`, which basically is a representation of the meta model classes of the AAS such as *Asset Administration Shell*, *Submodel*, *SubmodelElement*, or *Property*, and `Core`, which implements all the processing logic. 
Besides those two central components, FA³ST Service offers multiple interfaces that each can have different and/or custom implementations. 
FA³ST Service already ships with a number of so-called *default implementations* of these interfaces depicted by the light-grey boxes to the left and right in the figure.

```{figure} ../images/architecture.png
:width: 800px
:align: center
High-Level Architecture of FA³ST Service.
```

The interfaces provide the following functionalities:

- `Endpoint`:           Communication with the DT from the outside
- `MessageBus`:         Communication & synchronization between FA³ST Service components
- `De-/Serializer`:     De-/Serialization of AAS models in from/to data formats
- `Persistence`:        Persistent storage of data (model + values)
- `FileStorage`:        Peristent storage of complementary files (e.g. PDF files linked from the AAS)
- `AssetConnection`:    Synchronization with underlying asset(s)

