# Installation

## Requirements

-	Java Runtime 17 or newer

## Precompiled JAR

<!--start:download-release-->
{download}`Latest RELEASE version (1.2.0) <https://repo1.maven.org/maven2/de/fraunhofer/iosb/ilt/faaast/service/starter/1.2.0/starter-1.2.0.jar>`<!--end:download-release-->

<!--start:download-snapshot-->
{download}`Latest SNAPSHOT version (1.3.0-SNAPSHOT) <https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=de.fraunhofer.iosb.ilt.faaast.service&a=starter&v=1.3.0-SNAPSHOT>`<!--end:download-snapshot-->

## Maven Dependency

```xml
<dependency>
	<groupId>de.fraunhofer.iosb.ilt.faaast.service</groupId>
	<artifactId>starter</artifactId>
	<version>1.2.0</version>
</dependency>
```

## Gradle Dependency

```groovy
implementation 'de.fraunhofer.iosb.ilt.faaast.service:starter:1.2.0'
```

## Build from Source

```sh
git clone https://github.com/FraunhoferIOSB/FAAAST-Service
cd FAAAST-Service
mvn clean install
```