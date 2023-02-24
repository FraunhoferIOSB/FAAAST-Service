# About the Project

The Reference Architecture of Industrie 4.0 (RAMI) presents the [Asset Administration Shell (AAS)](https://www.plattform-i40.de/SiteGlobals/IP/Forms/Listen/Downloads/EN/Downloads_Formular.html?cl2Categories_TechnologieAnwendungsbereich_name=Verwaltungsschale) as the basis for interoperability. AAS is the digital representation of an asset that is able to provide information about this asset, i.e. information about properties, functionality, parameters, documentation, etc. The AAS operates as Digital Twin of the asset it represents.
Furthermore, the AAS covers all stages of the lifecycle of an asset starting in the development phase, reaching the most importance in the operation phase and finally delivering valuable information for the decline/decomposition phase.

To guarantee the interoperability of assets Industie 4.0 defines an information metamodel for the AAS covering all important aspects as type/instance concept, events, redefined data specification templates, security aspects, mapping of data formats and many more. Moreover interfaces and operations for a registry, a repository, publish and discovery are specified.
At first glance the evolving specification of the AAS seems pretty complex and a challenging task for asset providers. To make things easier, FA³ST provides an implementation of several tools to allow easy and fast creation and management of AAS-compliant Digital Twins.

## Roadmap

Next milestone is to release version 1.0.0 to Maven Central and DockerHub.

Some of the features we are working on include
-   improve stability/robustness
-   improve usability
-   additional implementations of the persistence interface
	-   file-based (✔️)
	-   database-backed

-   support for additional APIs
	-   Administration Shell Serialization Interface (✔️)
	-   AASX Server Interface

## Contact

faaast@iosb.fraunhofer.de

## License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.

Copyright (C) 2022 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131 Karlsruhe, Germany.

You should have received a copy of the Apache 2.0 License along with this program. If not, see [https://www.apache.org/licenses/LICENSE-2.0.html](https://www.apache.org/licenses/LICENSE-2.0.html).
