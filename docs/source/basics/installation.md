# Installation

## Requirements

-	Java Runtime 17 or newer

## Precompiled JAR

<!--start:download-release-->
{download}`Latest RELEASE version (1.3.0) <https://repo1.maven.org/maven2/de/fraunhofer/iosb/ilt/faaast/service/starter/1.3.0/starter-1.3.0.jar>`<!--end:download-release-->

<!--start:download-snapshot-->
<!--end:download-snapshot-->

## Maven Dependency

```xml
<dependency>
	<groupId>de.fraunhofer.iosb.ilt.faaast.service</groupId>
	<artifactId>starter</artifactId>
	<version>1.3.0</version>
</dependency>
```

## Gradle Dependency

```groovy
implementation 'de.fraunhofer.iosb.ilt.faaast.service:starter:1.3.0'
```

## Build from Source

```sh
git clone https://github.com/FraunhoferIOSB/FAAAST-Service
cd FAAAST-Service
mvn clean install
```