# FA³ST Service 

The **F**raunhofer **A**dvanced **A**sset **A**dministration **S**hell **T**ools (**FA³ST**) Service enables creation of Digital Twins (DTs) in accordance to the [Asset Administration Shell (AAS) specification ](https://industrialdigitaltwin.org/en/content-hub/aasspecifications).
FA³ST Service is a software that, when started, offers one or more AAS-compliant APIs to interact with a DT. 
Optionally, it can be started given an existing AAS model file and/or a configuration file.
FA³ST Service also allows synchronizing a DT with the asset(s) it represent via so-called *AssetConnection*.

```{figure} images/overview.png
:width: 700px
:align: center
FA³ST Service: Non-Technical View.
```

## Implemented AAS Specifications
- Details of the Asset Administration Shell - Part 1: Metamodel v3.0 ([specification](https://industrialdigitaltwin.org/en/content-hub/aasspecifications/specification-of-the-asset-administration-shell-part-1-metamodel-idta-number-01001-3-0))
- Details of the Asset Administration Shell - Part 2: Application Programming Interfaces v3.0.1 ([specification](https://industrialdigitaltwin.org/en/content-hub/aasspecifications/specification-of-the-asset-administration-shell-part-2-application-programming-interfaces-idta-number-01002-3-0)) ([OpenAPI](https://app.swaggerhub.com/apis/Plattform_i40/Entire-API-Collection/V3.0.1))



## Features

- Easy to use even for non-developers via command-line interface (CLI), docker container, or embedded library
- Configuration via a single JSON file
- Open Architecture: easily extendable and configurable
- Asset Synchronization: synchronize your assets and DTs using arbitrary communication protocols
- Allows accessing your DT using multiple endpoints at the same time, e.g., HTTPS and OPC UA
- Uses existing open source implementation of AAS datamodel and de-/serializers [Eclipse AAS4J](https://github.com/eclipse-aas4j/aas4j)
- Supports several dataformats for the Asset Administration Shell Environment: `AASX, JSON, XML`


:::{caution}
At the moment there is no security specification available for the AAS. Therefore FA³ST does not implement any security mechanisms. 
They will be implemented as soon as a security specification is available.
We strongly recommend to be careful when using external AAS models or submodels.
:::


```{toctree} 
:hidden:
:titlesonly:
:caption: Basics
basics/getting-started.md
basics/installation.md
basics/usage.md
basics/configuration.md
basics/faq.md
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
