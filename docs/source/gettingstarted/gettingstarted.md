# Getting Started

## Prerequisites

-   Java 11+

## Usage

### From precompiled JAR

<!--start:download-release-->
[Download latest RELEASE version (0.5.0)](https://repo1.maven.org/maven2/de/fraunhofer/iosb/ilt/faaast/service/starter/0.5.0/starter-0.5.0.jar)<!--end:download-release-->

<!--start:download-snapshot-->
[Download latest SNAPSHOT version (0.6.0-SNAPSHOT)](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=de.fraunhofer.iosb.ilt.faaast.service&a=starter&v=0.6.0-SNAPSHOT)<!--end:download-snapshot-->

### As Maven Dependency

```xml
<dependency>
	<groupId>de.fraunhofer.iosb.ilt.faaast.service</groupId>
	<artifactId>starter</artifactId>
	<version>0.5.0</version>
</dependency>
```

### As Gradle Dependency

```text
implementation 'de.fraunhofer.iosb.ilt.faaast.service:starter:0.5.0'
```

A maven plugin we are using in our build script leads to an error while resolving the dependency tree in gradle. Therefore you need to add following code snippet in your `build.gradle`. This code snippet removes the classifier of the transitive dependency `com.google.inject:guice`.

```text
configurations.all {
	resolutionStrategy.eachDependency { DependencyResolveDetails details ->
		if (details.requested.module.toString() == "com.google.inject:guice") {
			details.artifactSelection{
				it.selectArtifact(DependencyArtifact.DEFAULT_TYPE, null, null);
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

## Example

This example shows how to start a FA³ST Service given your custom AAS model (called `model.json` for simplicity but can be any relative or absolute path to an AAS model in any supported data format, e.g. JSON, XML, RDF or AASX). The service will expose an HTTP endpoint on default port 8080.

### Via Command-line Interface (CLI)

```sh
cd /starter/target
java -jar starter-{version}.jar -m model.json
```

### From Code (embedded)

```java
Service service = new Service(ServiceConfig.builder()
		.core(CoreConfig.builder()
				.requestHandlerThreadPoolSize(2)
				.build())
		.persistence(PersistenceInMemoryConfig.builder()
				.environment(AASEnvironmentHelper
						.fromFile(new File("{pathTo}\\FAAAST-Service\\misc\\examples\\demoAAS.json")))
				.build())
		.endpoint(HttpEndpointConfig.builder().build())
		.messageBus(MessageBusInternalConfig.builder().build())
		.build());
service.start();
```
