# Installation

Lore ipsum...

## Download

### Precompiled JAR

<!--start:download-release-->
[Download latest RELEASE version (0.5.0)](https://repo1.maven.org/maven2/de/fraunhofer/iosb/ilt/faaast/service/starter/0.5.0/starter-0.5.0.jar)<!--end:download-release-->

<!--start:download-snapshot-->
[Download latest SNAPSHOT version (0.6.0-SNAPSHOT)](https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=de.fraunhofer.iosb.ilt.faaast.service&a=starter&v=0.6.0-SNAPSHOT)<!--end:download-snapshot-->

### Maven Dependency

```xml
<dependency>
	<groupId>de.fraunhofer.iosb.ilt.faaast.service</groupId>
	<artifactId>starter</artifactId>
	<version>0.5.0</version>
</dependency>
```

### Gradle Dependency

```text
implementation 'de.fraunhofer.iosb.ilt.faaast.service:starter:0.5.0'
```

## Build from Source

### Prerequisites

-   Maven

```sh
git clone https://github.com/FraunhoferIOSB/FAAAST-Service
cd FAAAST-Service
mvn clean install
```