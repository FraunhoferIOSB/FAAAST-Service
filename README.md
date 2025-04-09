# FA³ST Service [![Build Status](https://github.com/FraunhoferIOSB/FAAAST-Service/workflows/Maven%20Build/badge.svg)](https://github.com/FraunhoferIOSB/FAAAST-Service/actions) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/25f6aafbdb0a4b5e8ba23672ec9411e5)](https://www.codacy.com/gh/FraunhoferIOSB/FAAAST-Service/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=FraunhoferIOSB/FAAAST-Service&amp;utm_campaign=Badge_Grade) [![Docker badge](https://img.shields.io/docker/pulls/fraunhoferiosb/faaast-service.svg)](https://hub.docker.com/r/fraunhoferiosb/faaast-service/) [![Documentation Status](https://readthedocs.org/projects/faaast-service/badge/?version=latest)](https://faaast-service.readthedocs.io/en/latest/?badge=latest) <a href="https://sonarcloud.io/summary/new_code?id=FraunhoferIOSB_FAAAST-Service" ><img src="https://sonarcloud.io/images/project_badges/sonarcloud-white.svg" alt="SonarCloud badge" width="105"/></a>

![FA³ST Logo Light](./docs/source/images/logo-positiv.png/#gh-light-mode-only "FA³ST Service Logo")
![FA³ST Logo Dark](./docs/source/images/logo-negativ.png/#gh-dark-mode-only "FA³ST Service Logo")

The **F**raunhofer **A**dvanced **A**sset **A**dministration **S**hell **T**ools (**FA³ST**) Service enables you to create digital twins based on the [Asset Administration Shell (AAS) specification](https://industrialdigitaltwin.org/en/content-hub/aasspecifications) with ease.
It is an implementation of the re-active or type 2 AAS, which means you can load existing AAS models and interact with them via API.
The features of FA³ST Service include

- free & open-source (Apache 2.0 license)
- native Java implementation
- easily extendable & configurable
- supports synchronization of the digital twins with existing assets using different protocols
- can be used as CLI, docker container, or embedded library


> [!TIP]
> For more details on FA³ST Service see the [:blue_book: **full documenation**](https://faaast-service.readthedocs.io/).


## Implemented AAS Specifications

- AAS Part 1: Metamodel v3.0 [Specification](https://industrialdigitaltwin.org/wp-content/uploads/2023/06/IDTA-01001-3-0_SpecificationAssetAdministrationShell_Part1_Metamodel.pdf)
- AAS Part 2: API v3.0.1 [Specification](https://industrialdigitaltwin.org/wp-content/uploads/2023/06/IDTA-01002-3-0_SpecificationAssetAdministrationShell_Part2_API_.pdf), [OpenAPI](https://app.swaggerhub.com/apis/Plattform_i40/Entire-API-Collection/V3.0.1)
- AAS Part 3a: Data Specification – IEC 61360 v3.0 [Specification](https://industrialdigitaltwin.org/en/wp-content/uploads/sites/2/2024/07/IDTA-01003-a-3-0-2_SpecificationAssetAdministrationShell_Part3a_DataSpecification_IEC613601.pdf)
- AAS Part 5: Package File Format (AASX) v3.0 [Specification](https://industrialdigitaltwin.org/en/wp-content/uploads/sites/2/2024/06/IDTA-01005-3-0-1_SpecificationAssetAdministrationShell_Part5_AASXPackageFileFormat.pdf)


## Usage

> [!IMPORTANT]
> At the moment there is no security specification available for the AAS.
> Therefore FA³ST Service does not implement any security mechanisms.
> They will be implemented as soon as a security specification is available.
> We strongly recommend to be careful when using external AAS models or submodels.

### Download pre-compiled JAR

<!--start:download-release-->
[Download latest RELEASE version (1.2.0)](https://repo1.maven.org/maven2/de/fraunhofer/iosb/ilt/faaast/service/starter/1.2.0/starter-1.2.0.jar)<!--end:download-release-->

<!--start:download-snapshot-->
[Download latest SNAPSHOT version (1.3.0-SNAPSHOT)](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=de.fraunhofer.iosb.ilt.faaast.service&a=starter&v=1.3.0-SNAPSHOT)<!--end:download-snapshot-->

### As Maven Dependency
```xml
<dependency>
	<groupId>de.fraunhofer.iosb.ilt.faaast.service</groupId>
	<artifactId>starter</artifactId>
	<version>1.2.0</version>
</dependency>
```

### As Gradle Dependency
```kotlin
implementation 'de.fraunhofer.iosb.ilt.faaast.service:starter:1.2.0'
```

## Building from Source

```sh
git clone https://github.com/FraunhoferIOSB/FAAAST-Service
cd FAAAST-Service
mvn clean install
```

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions are **greatly appreciated**.
You can find our contribution guidelines [here](CONTRIBUTING.md)

## Contact

faaast@iosb.fraunhofer.de

## License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.

Copyright (C) 2022 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131 Karlsruhe, Germany.

You should have received a copy of the Apache 2.0 License along with this program. If not, see https://www.apache.org/licenses/LICENSE-2.0.html.
