# FA³ST Service 
![FA³ST Logo Light](./images/Fa3st-Service_positiv.png "FA³ST Service Logo")

The **F**raunhofer **A**dvanced **A**sset **A**dministration **S**hell **T**ools (**FA³ST**) Service implements the [Asset Administration Shell (AAS) specification from the platform Industrie 4.0](https://www.plattform-i40.de/SiteGlobals/IP/Forms/Listen/Downloads/EN/Downloads_Formular.html?cl2Categories_TechnologieAnwendungsbereich_name=Verwaltungsschale) and builds an easy-to-use web service based on a custom AAS model instance.

**Implemented AAS versions**
| Part | Version | Comment |
|:--| -- | -- |
| Part 1 - The exchange of information between partners in the value chain of Industrie 4.0 | Version 3.0RC01* | * We are using the AAS model java implementation from [admin-shell-io](https://github.com/admin-shell-io/java-model) which is based on Version 3.0RC01 but also covers already some aspects from RC02 |
| Part 2 - Interoperability at Runtime - Exchanging Information via Application Programming Interfaces | Version 1.0RC02 |  |

## Features

FA³ST Service provides the following functionalities:
-   supports several dataformats for the Asset Administration Shell Environment: `json, json-ld, xml, aml, rdf, opcua nodeset`
-   easy configuration via JSON file
-   easily expandable with 3rd party implementations for `endpoint, messagebus, persistence, assetconnection`
-   uses existing open source implementation of AAS datamodel and de-/serializers [admin-shell-io java serializer](https://github.com/admin-shell-io/java-serializer) and [admin-shell-io java model](https://github.com/admin-shell-io/java-model)
-   synchronization between multiple endpoints
-   connecting to assets using arbitrary communication protocols


```{toctree} 
:caption: Getting Started
:maxdepth: 3
gettingstarted/gettingstarted.md
gettingstarted/commandline.md
gettingstarted/configuration.md
gettingstarted/docker.md
```

```{toctree} 
:caption: Architecture
:maxdepth: 3
architecture
```

```{toctree} 
:caption: Endpoints
:maxdepth: 3
endpoints/http_endpoint.md
endpoints/opcua_endpoint.md
```

```{toctree} 
:caption: Asset Connections
:maxdepth: 3
assetconnetions/assetconnection.md
assetconnetions/http_assetconnection.md
assetconnetions/opcua_assetconnection.md
assetconnetions/mqtt_assetconnection.md
```

```{toctree} 
:caption: Persistence
:maxdepth: 3
persistence/persistence.md
persistence/inmemory_persistence.md
persistence/file_persistence.md
```

```{toctree} 
:caption: About
:maxdepth: 2
about/about.md
about/contributing.md
about/recommended.md
```
