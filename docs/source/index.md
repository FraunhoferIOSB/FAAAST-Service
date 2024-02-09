# FA³ST Service 
![FA³ST Logo Light](./images/Fa3st-Service_positiv.png "FA³ST Service Logo")

The **F**raunhofer **A**dvanced **A**sset **A**dministration **S**hell **T**ools (**FA³ST**) Service implements the [Asset Administration Shell (AAS) specification by Plattform Industrie 4.0](https://www.plattform-i40.de/SiteGlobals/IP/Forms/Listen/Downloads/EN/Downloads_Formular.html?cl2Categories_TechnologieAnwendungsbereich_name=Verwaltungsschale) and provides an easy-to-use re-active AAS (Type 2) hosting custom AAS models.

## Implemented AAS Specifications
| Specification | Version |
|:--| -- |
| Details of the Asset Administration Shell - Part 1<br />The exchange of information between partners in the value chain of Industrie 4.0 | Version 3.0
| Details of the Asset Administration Shell - Part 2<br />Interoperability at Runtime – Exchanging Information via Application Programming Interfaces | Version 3.0.1<br />([specification](https://www.plattform-i40.de/IP/Redaktion/EN/Downloads/Publikation/Details_of_the_Asset_Administration_Shell_Part2_V1.pdf)) |

## Features

-   supports several dataformats for the Asset Administration Shell Environment: `AASX, JSON, XML`
-   easy configuration via JSON file
-   easily expandable with 3rd party implementations for `endpoint, messagebus, persistence, assetconnection`
-   uses existing open source implementation of AAS datamodel and de-/serializers [Eclipse AAS4J](https://github.com/eclipse-aas4j/aas4j)
-   synchronization between multiple endpoints
-   connecting to assets using arbitrary communication protocols
-   can be used via command-line interface (CLI), as docker container or embedded library


```{toctree} 
:hidden:
:caption: Basics
basics/getting-started.md
basics/installation.md
basics/usage.md
```

```{toctree} 
:hidden:
:caption: Interfaces
interfaces/endpoint.md
interfaces/asset-connection.md
interfaces/persistence.md
interfaces/file-storage.md
interfaces/message-bus.md
```

```{toctree} 
:hidden:
:caption: Other
other/release-notes.md
other/about-the-project.md
other/contributing.md
other/recommended-documents-and-links.md
```
