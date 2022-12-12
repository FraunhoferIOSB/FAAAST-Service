# FA³ST Service [![Build Status](https://github.com/FraunhoferIOSB/FAAAST-Service/workflows/Maven%20Build/badge.svg)](https://github.com/FraunhoferIOSB/FAAAST-Service/actions) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/25f6aafbdb0a4b5e8ba23672ec9411e5)](https://www.codacy.com/gh/FraunhoferIOSB/FAAAST-Service/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=FraunhoferIOSB/FAAAST-Service&amp;utm_campaign=Badge_Grade) [![Docker badge](https://img.shields.io/docker/pulls/fraunhoferiosb/faaast-service.svg)](https://hub.docker.com/r/fraunhoferiosb/faaast-service/) [![Documentation Status](https://readthedocs.org/projects/faaast-service/badge/?version=latest)](https://faaast-service.readthedocs.io/en/latest/?badge=latest) <a href="https://sonarcloud.io/summary/new_code?id=FraunhoferIOSB_FAAAST-Service" ><img src="https://sonarcloud.io/images/project_badges/sonarcloud-white.svg" alt="SonarCloud badge" width="105"/></a>

![FA³ST Logo Light](./docs/source/images/Fa3st-Service_positiv.png/#gh-light-mode-only "FA³ST Service Logo")
![FA³ST Logo Dark](./docs/source/images/Fa3st-Service_negativ.png/#gh-dark-mode-only "FA³ST Service Logo")

The **F**raunhofer **A**dvanced **A**sset **A**dministration **S**hell **T**ools (**FA³ST**) Service implements the [Asset Administration Shell (AAS) specification by Plattform Industrie 4.0](https://www.plattform-i40.de/SiteGlobals/IP/Forms/Listen/Downloads/EN/Downloads_Formular.html?cl2Categories_TechnologieAnwendungsbereich_name=Verwaltungsschale) and provides an easy-to-use re-active AAS (Type 2) hosting custom AAS models. If you are not familiar with AAS you can find additional information [here](https://www.plattform-i40.de/IP/Redaktion/EN/Standardartikel/specification-administrationshell.html).

For more details on FA³ST Service see the full documenation :blue_book: [here](https://faaast-service.readthedocs.io/).

| FA³ST Service is still under development. Contributions in form of issues and pull requests are highly welcome. |
|-----------------------------|

## Implemented AAS Specifications
| Specification | Version |
|:--| -- |
| Details of the Asset Administration Shell - Part 1<br />The exchange of information between partners in the value chain of Industrie 4.0 | Version 3.0RC01<br />(based on [admin-shell-io/java-model](https://github.com/admin-shell-io/java-model))
| Details of the Asset Administration Shell - Part 2<br />Interoperability at Runtime – Exchanging Information via Application Programming Interfaces | Version 1.0RC02 |

## Prerequisites

-   Java 11+

## Getting Started

You can find a detailled documentation :blue_book: [here](https://faaast-service.readthedocs.io/)

## Usage

### Download pre-compiled JAR

<!--start:download-release-->
[Download latest RELEASE version (0.3.0)](https://repo1.maven.org/maven2/de/fraunhofer/iosb/ilt/faaast/service/starter/0.3.0/starter-0.3.0.jar)<!--end:download-release-->

<!--start:download-snapshot-->
<!--end:download-snapshot-->

### As Maven Dependency
```xml
<dependency>
	<groupId>de.fraunhofer.iosb.ilt.faaast.service</groupId>
	<artifactId>starter</artifactId>
	<version>0.3.0</version>
</dependency>
```

### As Gradle Dependency
```kotlin
implementation 'de.fraunhofer.iosb.ilt.faaast.service:starter:0.3.0'
```

A maven plugin we are using in our build script leads to an error while resolving the dependency tree in gradle. Therefore you need to add following code snippet in your `build.gradle`. This code snippet removes the classifier of the transitive dependency `com.google.inject:guice`.
```kotlin
configurations.all {
	resolutionStrategy.eachDependency { DependencyResolveDetails details ->
		if (details.requested.module.toString() == "com.google.inject:guice") {
			details.artifactSelection{
				it.selectArtifact(DependencyArtifact.DEFAULT_TYPE, null, null)
			}
		}
	}
}
```

## Building from Source

### Prerequisites

-   Maven

```sh
git clone https://github.com/FraunhoferIOSB/FAAAST-Service
cd FAAAST-Service
mvn clean install
```

## Changelog

You can find the detailed changelog [here](docs/source/changelog/changelog.md).

## Roadmap

Next milestone is to release version 1.0.0 to Maven Central and DockerHub.

Some of the features we are working on include
-   improve stability/robustness
-   improve usability
-   additional implementations of the persistence interface
	-   file-based (:heavy_check_mark:)
	-   database-backed

-   support for additional APIs
	-   Administration Shell Serialization Interface (:heavy_check_mark:)
	-   AASX Server Interface

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions are **greatly appreciated**.
You can find our contribution guidelines [here](CONTRIBUTING.md)

## Contributors

| Name | Github Account |
|:--| -- |
| Michael Jacoby | [mjacoby](https://github.com/mjacoby) |
| Jens Müller | [JensMueller2709](https://github.com/JensMueller2709) |
| Klaus Schick | [schick64](https://github.com/schick64) |
| Tino Bischoff | [tbischoff2](https://github.com/tbischoff2) |
| Friedrich Volz | [fvolz](https://github.com/fvolz) |

## Contact

faaast@iosb.fraunhofer.de

## License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.

Copyright (C) 2022 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131 Karlsruhe, Germany.

You should have received a copy of the Apache 2.0 License along with this program. If not, see https://www.apache.org/licenses/LICENSE-2.0.html.
