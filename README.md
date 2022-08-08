# FA³ST Service [![Build Status](https://github.com/FraunhoferIOSB/FAAAST-Service/workflows/Maven%20Build/badge.svg)](https://github.com/FraunhoferIOSB/FAAAST-Service/actions) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/25f6aafbdb0a4b5e8ba23672ec9411e5)](https://www.codacy.com/gh/FraunhoferIOSB/FAAAST-Service/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=FraunhoferIOSB/FAAAST-Service&amp;utm_campaign=Badge_Grade) [![Docker badge](https://img.shields.io/docker/pulls/fraunhoferiosb/faaast-service.svg)](https://hub.docker.com/r/fraunhoferiosb/faaast-service/) <a href="https://sonarcloud.io/summary/new_code?id=FraunhoferIOSB_FAAAST-Service" ><img src="https://sonarcloud.io/images/project_badges/sonarcloud-white.svg" alt="SonarCloud badge" width="105"/></a>

![FA³ST Logo Light](./docs/images/Fa3st-Service_positiv.png/#gh-light-mode-only "FA³ST Service Logo")
![FA³ST Logo Dark](./docs/images/Fa3st-Service_negativ.png/#gh-dark-mode-only "FA³ST Service Logo")

The **F**raunhofer **A**dvanced **A**sset **A**dministration **S**hell **T**ools (**FA³ST**) Service implements the [Asset Administration Shell (AAS) specification from the platform Industrie 4.0](https://www.plattform-i40.de/SiteGlobals/IP/Forms/Listen/Downloads/EN/Downloads_Formular.html?cl2Categories_TechnologieAnwendungsbereich_name=Verwaltungsschale) and builds an easy-to-use web service based on a custom AAS model instance. If you are not familiar with AAS you can find additional information [here](#about-the-project).

| FA³ST Service is still under development. Contributions in form of issues and pull requests are highly welcome. |
|-----------------------------|

<b>Implemented AAS versions</b>
| Part | Version | Comment |
|:--| -- | -- |
| Part 1 - The exchange of information between partners in the value chain of Industrie 4.0 | Version 3.0RC01* | * We are using the AAS model java implementation from [admin-shell-io](https://github.com/admin-shell-io/java-model) which is based on Version 3.0RC01 but also covers already some aspects from RC02 |
| Part 2 – Interoperability at Runtime – Exchanging Information via Application Programming Interfaces | Version 1.0RC02 |  |

:blue_book: **Documentation** See full documenation [here](https://faaast-service.readthedocs.io/)

## Getting Started

This is an example of how to set up your project locally.
To get a local copy up and running follow these simple example steps.
To compile the FA³ST service you need to have a JDK and Maven installed.

### Prerequisites

-   Java 11+
-   Maven

### Building from Source

```sh
git clone https://github.com/FraunhoferIOSB/FAAAST-Service
cd FAAAST-Service
mvn clean install
```

#### As Maven Dependency
```xml
<dependency>
	<groupId>de.fraunhofer.iosb.ilt.faaast.service</groupId>
	<artifactId>starter</artifactId>
	<version>0.1.0</version>
</dependency>
```

#### As Gradle Dependency
```kotlin
implementation 'de.fraunhofer.iosb.ilt.faaast.service:starter:0.1.0'
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

### Example

The following code starts a FA³ST Service with a HTTP endpoint on port 8080.

```java
String pathToYourAASEnvironment = "{pathTo}\\FAAAST-Service\\misc\\examples\\demoAAS.json";
AssetAdministrationShellEnvironment environment = AASEnvironmentHelper.fromFile(new File(pathToYourAASEnvironment));
Service service = new Service(environment,
	new ServiceConfig.Builder()
		.core(new CoreConfig.Builder()
			.requestHandlerThreadPoolSize(2)
			.build())
		.persistence(new PersistenceInMemoryConfig())
		.endpoint(new HttpEndpointConfig())
		.messageBus(new MessageBusInternalConfig())
		.build());
service.start();
```
Afterwards, you can reach the running FA³ST Service via `http://localhost:8080/shells`.

## Roadmap

Next milestone is to publish a first 1.0.0 release to Maven Central and DockerHub.
Some of the features we are working on include
-   improve stability/robustness
-   improve usability
-   implement a file & database persistence in FA³ST Service
-   implement the AASX Server interface
-   implement the Asset Administration Shell Serialization interface

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions are **greatly appreciated**.

If you have a suggestion for improvements, please fork the repo and create a pull request. You can also simply open an issue.
Don't forget to rate the project! Thanks again!

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

### Code Formatting
The project uses _spotless:check_ in the build cycle, which means the project only compiles if all code, *.pom and *.xml files are formatted according to the project's codestyle definitions (see details on [spotless](https://github.com/diffplug/spotless)).
You can automatically format your code by running

>mvn spotless:apply

Additionally, you can import the eclipse formatting rules defined in _/codestyle_ into our IDE.

### Third Party License
If you use additional dependencies please be sure that the licenses of these dependencies are compliant with our [License](#license). If you are not sure which license your dependencies have, you can run
>mvn license:aggregate-third-party-report

and check the generated report in the directory `documentation/third_party_licenses_report.html`.

<p align="right">(<a href="#top">back to top</a>)</p>

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

<p align="right">(<a href="#top">back to top</a>)</p>

## License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.

Copyright (C) 2022 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131 Karlsruhe, Germany.

You should have received a copy of the Apache 2.0 License along with this program. If not, see https://www.apache.org/licenses/LICENSE-2.0.html.

<p align="right">(<a href="#top">back to top</a>)</p>
