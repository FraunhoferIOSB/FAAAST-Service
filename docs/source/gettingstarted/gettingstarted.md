SOme info text about getting started
## Prerequisites

Java 17

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

One of the maven plugins in our build script leads to an error while resolving the dependency tree in gradle. Therefore, you need to add the following code snippet in your `build.gradle`. This code snippet removes the classifier of the transitive dependency `com.google.inject:guice`.

## Building from Source

### Prerequisites

-   Maven

```sh
git clone https://github.com/FraunhoferIOSB/FAAAST-Service
cd FAAAST-Service
mvn clean install
```

## Example

This example shows how to start a FA³ST Service given your custom AAS model (called `model.json` for simplicity but can be any relative or absolute path to an AAS model in any supported data format, e.g. JSON, XML, or AASX). The service will expose an HTTP endpoint on default port 8080.

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
		.initialModelFile(new File("{pathTo}\\FAAAST-Service\\misc\\examples\\model.aasx"))
		.build())
	.endpoint(HttpEndpointConfig.builder().build())
	.messageBus(MessageBusInternalConfig.builder().build())
	.fileStorage(FileStorageInMemoryConfig.builder().build())
	.build());
service.start();
```
