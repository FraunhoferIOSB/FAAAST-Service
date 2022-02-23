
# FA³ST Service

![FA³ST Logo Light](./documentation/images/Fa3st-Service_positiv.png/#gh-light-mode-only "FA³ST Service Logo")

![FA³ST Logo Dark](./documentation/images/Fa3st-Service_negativ.png/#gh-dark-mode-only "FA³ST Service Logo")

The **F**raunhofer **A**dvanced **A**sset **A**dministration **S**hell **T**ools (**FA³ST**) Service implements the [Asset Administration Shell (AAS) specification from the platform Industrie 4.0](https://www.plattform-i40.de/SiteGlobals/IP/Forms/Listen/Downloads/EN/Downloads_Formular.html?cl2Categories_TechnologieAnwendungsbereich_name=Verwaltungsschale) and built up an easy-to-use Web Service based on a custom AAS model instance. If you are not familiar with AAS have a look [here](#about-the-project).

| Currently we publish the FA³ST Service here as <mark>BETA Version</mark> since not all functionalities fully tested yet and some internal capabilities like logging and exception handling are still missing. However, contributions like bug issues or questions are highly welcome. |
|-----------------------------|


<!-- GETTING STARTED -->
## Getting Started

This is an example of how you may setting up your project locally.
To get a local copy up and running follow these simple example steps.
To compile FA³ST-Service you need to have a JDK and Maven installed.

### Installation

The following commands will build the most recent version of the FA³ST-Service from git main. The compiled package will then be in the "[module]/target" directory.
```sh
git clone https://github.com/FraunhoferIOSB/FAAAST-Service
cd FAAAST-Service
mvn package
```
Alternatively, use `mvn install` instead of `mvn package` to build a SNAPSHOT jar and then copy it into your local repository, so that you can use it in your Maven projects.

To start the Service from command line do following commands.
```sh
cd /starter
mvn clean package
cd /target
java -jar starter-{version}.jar -e {path/to/your/AASEnvironment}
```
For further information of using the command line see [here](#usage-with-command-line).

### Example

Since the FA³ST Service is not yet published to maven central (we are planning to do that), the FA³ST Service needs to be installed manually to your local maven repository. Afterwards, you can add the FA³ST Service module `starter` to your project.

```xml
<dependency>
	<groupId>de.fraunhofer.iosb.ilt.faaast.service</groupId>
	<artifactId>starter</artifactId>
	<version>1.0.0-SNAPSHOT</version>
</dependency>
```

The following code starts a FA³ST Service with a HTTP endpoint on port 8080. You can use the `AASFull.json` as an example AASEnvironment or use your own AASEnvironment. Therefore, just replace the `pathToYourAASEnvironment` with the path to your file.
```java
String pathToYourAASEnvironment = "{pathTo}\\FAAAST-Service\\starter\\src\\test\\resources\\AASFull.json";
Service service = new Service(
		new ServiceConfig.Builder()
				.core(new CoreConfig.Builder()
						.requestHandlerThreadPoolSize(2)
						.build())
				.persistence(new PersistenceInMemoryConfig())
				.endpoints(List.of(new HttpEndpointConfig()))
				.messageBus(new MessageBusInternalConfig())
				.build());
service.setAASEnvironment(new AASEnvironmentFactory()
		.getAASEnvironment(pathToYourAASEnvironment));
service.start();
```
Afterwards, you can reach the running FA³ST Service with `http://localhost:8080/shells`.

<p align="right">(<a href="#top">back to top</a>)</p>

## Features

FA³ST Service provides a number of useful functionalities:
- The FA³ST Service supports several dataformats for the Asset Administration Shell Environment: `json, json-ld, xml, aml, rdf, opcua nodeset`
- The FA³ST Service can be easily configured by a configuration file in json format. Additionally it is possible to use default configurations.
- The FA³ST Service uses the results of well known [admin-shell-io java serializer](https://github.com/admin-shell-io/java-serializer) and [admin-shell-io java model](https://github.com/admin-shell-io/java-model). That´s why we can react very fast on changes in the AAS specification.
- The FA³ST Service can be easily expanded with own implementations for `endpoint, messagebus, persistence, assetconnection`. Therefore, interfaces are provided in the `core` which needs to be implemented.
<img src="./documentation/images/fa3st-service-default-implementations.png" alt="FA³ST Service Logo" >

- The FA³ST Service provides for each component one or more default implementations
- In memory persistence in class `de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemory`
- Internal messagebus in class `de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternal`
- HTTP Endpoint in class `de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint`
- OPC UA Endpoint in class `de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys.OpcUaEndpoint`
- MQTT Assetconnection in class `de.fraunhofer.iosb.ilt.faaast.service.assetconnection.mqtt.MqttAssetConnection`
- OPC UA Assetconnection in class `de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.OpcUaAssetConnection`
- The FA³ST Service can be started with multiple synchronized endpoints in parallel
- [HTTP Endpoint](#http-endpoint-interface)
- [OPCUA Endpoint](#opc-ua-endpoint-interface)
- The FA³ST Service provides the interface `AssetConnection` to easily connect your AssetAdministrationShell to your asset.

## Usage with Command Line
Here you get a short introduction about starting the FA³ST Service through the command line. To get all capabilities like using environment variables check the [full documentation](./documentation/commandline.md).

To start a FA³ST Service from the command line:
1. Move to the starter project and build the project
	```sh
	cd /starter
	mvn clean package
	```
2. Move to the generated `.jar` file
	```sh
	cd starter/target
	```
3. Execute the `.jar` file to start a FA³ST Service directly with a default configuration. Replace the `{path/to/your/AASEnvironment}` with your file to the Asset Administration Shell Environment you want to load with the FA³ST Service. If you just want to play around, you can use a example AASEnvironment from us [here](starter/src/test/resources/AASFull.json).
	```sh
	java -jar starter-{version}.jar -e {path/to/your/AASEnvironment}
	```

Currently we supporting following formats of the Asset Administration Shell Environment model:
>json, json-ld, aml, xml, opcua nodeset, rdf
<p>

Following command line parameters could be used:
```
-c, --configFile=<configFilePath>
						The config file path. Default Value = config.json

-e, --environmentFile=<aasEnvironmentFilePath>
						Asset Administration Shell Environment FilePath.
						Default Value = aasenvironment.*

--emptyEnvironment   	Starts the FA³ST service with an empty Asset
						Administration Shell Environment. False by default

--endpoints[=<endpoints>...]
						Supported endpoints: http

-h, --help              Show this help message and exit.

--[no-]autoCompleteConfig
						Autocompletes the configuration with default values
						for required configuration sections. True by default

--[no-]modelValidation
						Validates the AAS Environment. True by default

-V, --version           Print version information and exit.
```
<hr>

## Usage with Docker
When using Docker there are two options to get an AAS running with FA³ST.

<mark>Currently the model validation is deactivated in all examples</mark>

### Docker-Compose
Clone this repository, navigate to `/misc/docker/` and run this command inside it.
```sh
docker-compose up -d
```
To use your own AAS environment just replace the `/misc/docker/examples/demoAAS.json`. If you want to modify the configuration of the FA³ST service, change the contents of `/misc/docker/examples/exampleConfiguration.json`.

### Docker CLI
To start the FA³ST service with an empty AAS environment execute this command.
```sh
docker run --rm -P fraunhoferiosb/faaast-service '--emptyEnvironment' '--no-modelValidation'
```
To start the FA³ST service with your own AAS environment, just place the JSON-file (in this example `demoAAS.json`) containing your enviroment in the current directory, just modify the command accordingly and run it.
```sh
docker run --rm -v "$(pwd)"/demoAAS.json:/AASEnv.json -e faaast.aasEnvFilePath=AASEnv.json -P fraunhoferiosb/faaast-service '--no-modelValidation'
```
Similarly to the above examples you can pass more arguments to the FA³ST service by using the CLI or a configuration file as provided in the cfg folder (use the `faaast.configFilePath` environment variable for that).

## Components

### Configuration
<!--TBD-->
Here you get a short introduction about the configuration of the FA³ST Service. To get all capabilities like creating your own configuration for your implementation check the [full documentation](./documentation/configuration.md).

The FA³ST Service can be easily configurated by a config file when starting via the command line or via code. Following you find an example configuration file. The `@class` attribute defines which implementation class should be loaded for this component. Each implementation class can define its own attributes in his configuration which can be set in the configuration file. For instance, the `HttpEndpoint` defines in his configuration implementation a attribute named `port`. This attribute can now be set in the configuration file.
```json
{
"core" : {
	"requestHandlerThreadPoolSize" : 2
},
"endpoints" : [ {
	"@class" : "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint",
	"port" : 8080
} ],
"persistence" : {
	"@class" : "de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemory"
},
"messageBus" : {
	"@class" : "de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternal"
}
}
```

In the following code snippet you see how to create a `ServiceConfig` object which you need to instantiate a service:
```java
ServiceConfig serviceConfig = new ServiceConfig.Builder()
	.core(new CoreConfig.Builder()
			.requestHandlerThreadPoolSize(2)
			.build())
	.persistence(new PersistenceInMemoryConfig())
	.endpoints(List.of(new HttpEndpointConfig()))
	.messageBus(new MessageBusInternalConfig())
	.build());
```


### HTTP-Endpoint Interface
Here you get a short introduction about the HTTP Endpoint of the FA³ST Service. To get all capabilities check the [full documentation](./documentation/httpendpoint.md).

The HTTP-Endpoint allows accessing data and execute operations within the FA³ST-Service via REST-API.
The HTTP-Endpoint Interface is based on the document [Details of the Asset Administration Shell - Part 2](https://www.plattform-i40.de/IP/Redaktion/EN/Downloads/Publikation/Details_of_the_Asset_Administration_Shell_Part2_V1.html), _Interoperability at Runtime –
Exchanging Information via Application
Programming Interfaces (Version 1.0RC02)_' , November 2021 and the OpenAPI documentation [DotAAS Part 2 | HTTP/REST | Entire Interface Collection](https://app.swaggerhub.com/apis/Plattform_i40/AssetAdministrationShell-REST-API/Final-Draft), Nov, 11th 2021

For detailed information on the REST API see
[DotAAS Part 2 | HTTP/REST | Entire Interface Collection](https://app.swaggerhub.com/apis/Plattform_i40/AssetAdministrationShell-REST-API/Final-Draft), Nov, 11th 2021

In order to use the HTTP-Endpoint the configuration settings require to include an HTTP-Endpoint configuration, like the one below:
```json
{
	"endpoints": [
		{
			"@class": "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint",
			"port": 8080
		}
	]
}
```
<p align="right">(<a href="#top">back to top</a>)</p>

### HTTP Example
Sample HTTP-Call for Operation _GetSubmodelElementByPath_ (returns a specific submodel element from the Submodel at a specified path) using the parameters
- _submodelIdentifier_: https://acplt.org/Test_Submodel (must be base64URL-encoded)
- _idShortPath_: ExampleRelationshipElement (must be URL-encoded)

and the query-parameters _level=deep_ and _content=normal_.

```sh
http://localhost:8080/submodels/aHR0cHM6Ly9hY3BsdC5vcmcvVGVzdF9TdWJtb2RlbA==/submodel/submodel-elements/ExampleRelationshipElement?level=deep&content=normal
```

### The following interface URLs are currently not (yet) supported:
- Submodel Repository Interface (Alternative Interface URLs "Swagger Doc Feb. 2022",
[DotAAS Part 2 | HTTP/REST | Asset Administration Shell Repository](https://app.swaggerhub.com/apis/Plattform_i40/AssetAdministrationShell-Repository/Final-Draft#/Asset%20Administration%20Shell%20Repository/GetSubmodel) (yet not fully specified))
> /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/...

- AASX File Server Interface (not yet supported)
> /packages

- Asset Administration Shell Serialization Interface (not yet supported)
> /serialization (GET)

- Asset Administration Shell Basic Discovery (not yet supported)
> /lookup/shells

<p align="right">(<a href="#top">back to top</a>)</p>


### OPC UA Endpoint Interface
Here you get a short introduction about the OPCUA Endpoint of the FA³ST Service. To get all capabilities check the [full documentation](./documentation/opcuaendpoint.md).

The OPC UA Endpoint allows accessing data and execute operations within the FA³ST-Service via OPC UA.
For detailed information on OPC UA see
[About OPC UA](https://opcfoundation.org/about/opc-technologies/opc-ua/)

The OPC UA Endpoint is based on the [OPC UA Companion Specification OPC UA for Asset Administration Shell (AAS)](https://opcfoundation.org/developer-tools/specifications-opc-ua-information-models/opc-ua-for-i4-asset-administration-shell/).
The release version of this Companion Specification is based on the document [Details of the Asset Administration Shell - Part 1 Version 2](https://www.plattform-i40.de/IP/Redaktion/EN/Downloads/Publikation/Details_of_the_Asset_Administration_Shell_Part1_V2.html).

This implementation is based on [Details of the Asset Administration Shell - Part 1 Version 3](https://www.plattform-i40.de/IP/Redaktion/EN/Downloads/Publikation/Details_of_the_Asset_Administration_Shell_Part1_V3.html), which is currently not yet released.
Therefore, the current implementation is actually not compatible with the Companion Specification.

The OPC UA Endpoint is built with the [Prosys OPC UA SDK for Java](https://www.prosysopc.com/products/opc-ua-java-sdk/).
If you want to build the OPC UA Endpoint, you need a valid license for the SDK.

You can purchase a [Prosys OPC UA License](https://www.prosysopc.com/products/opc-ua-java-sdk/purchase/). As the OPC UA Endpoint is a server, you need a "Client & Server" license.

For evaluation purposes, you also have the possibility to request an [evaluation license](https://www.prosysopc.com/products/opc-ua-java-sdk/evaluate).

<p align="right">(<a href="#top">back to top</a>)</p>

### MQTT AssetConnection
- Short introduction
- Small Example with configuration

### OPC UA AssetConnection
- Short introduction
- Small Example with configuration

### In Memory Persistence
Not yet implemented:
- AASX Packages
- Package Descriptors
- SubmodelElementStructs

<hr>

## About the Project
The Reference Architecture of Industrie 4.0 (RAMI) presents the [Asset Administration Shell (AAS)](https://www.plattform-i40.de/SiteGlobals/IP/Forms/Listen/Downloads/EN/Downloads_Formular.html?cl2Categories_TechnologieAnwendungsbereich_name=Verwaltungsschale) as the basis for interoperability. AAS is the digital representation of an asset that is able to provide information about this asset, i.e. information about properties, functionality, parameters, documentation, etc.. The AAS operates as Digital Twin of the asset it represents.
Furthermore, the AAS covers all stages of the lifecycle of an asset starting in the development phase, reaching the most importance in the operation phase and finally delivering valuable information for the decline/decomposition phase.

To guarantee the interoperability of assets Industie 4.0 defines an information metamodel for the AAS covering all important aspects as type/instance concept, events, redefined data specification templates, security aspects, mapping of data formats and many more. Moreover interfaces and operations for a registry, a repository, publish and discovery are specified.
At first glance the evolving specification of the AAS seems pretty complex and a challenging task for asset providers. To make things easier to FA³ST provides an implementation of several tools to allow easy and fast creation and management of AAS-compliant Digital Twins.

<p align="right">(<a href="#top">back to top</a>)</p>

<!--TO BE DISCUSSED -->
## Roadmap

We will continously expand the features of the FA³ST environment. However, we highly welcome bug reports, feature requests, code contributions, and assistance with testing. Our next steps and implementations will be:
- Implement a file & database persistence in FA³ST Service
- Expand the functionality and configuration options in the MQTT/OPCUA AssetConnections in FA³ST Service
- Implement an AAS Registry called FA³ST Registry

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue.
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Formatting
The project uses *spotless:check* in the build cycle, which means the project only compiles if all code, *.pom and *.xml files are formatted according to the project's codestyle definitions (see details on [spotless](https://github.com/diffplug/spotless)).
You can automatically format your code by running

> mvn spotless:apply

Additionally, you can import the eclipse formatting rules defined in */codestyle* into our IDE.

### Third Party License
If you use additional dependencies please be sure that the dependency License are compliant with our [License](#license). If you are not sure which License your dependencies have, you can run
> mvn license:aggregate-third-party-report

and check the generated report in the directory `./documentation/third_party_licenses_report.html`.

<p align="right">(<a href="#top">back to top</a>)</p>

## Recommended Documents/Links
* [Asset Administration Shell Specifications](https://www.plattform-i40.de/IP/Redaktion/EN/Standardartikel/specification-administrationshell.html) <br />
Quicklinks To Different Versions & Reading Guide
* [Details of the Asset Administration Shell - Part 1](https://www.plattform-i40.de/IP/Redaktion/EN/Downloads/Publikation/Details_of_the_Asset_Administration_Shell_Part1_V3.html), Nov 2021 <br />
The publication states how companies can use the Asset Administration Shell to compile and structure information. In this way all information can be shared as a package (set of files) with partners at several levels of the value chain. It is not necessary to provide online access to this data from the very beginning.
* [Details of the Asset Administration Shell - Part 2](https://www.plattform-i40.de/IP/Redaktion/EN/Downloads/Publikation/Details_of_the_Asset_Administration_Shell_Part2_V1.html), Nov 2021 <br />
This part extends Part 1 and defines how information provided in the Asset Administration Shell (AAS) (e.g. submodels or properties) can be accessed dynamically via Application Programming Interfaces (APIs).
* [About OPC UA](https://opcfoundation.org/about/opc-technologies/opc-ua/) <br />
* [OPC UA Companion Specification OPC UA for Asset Administration Shell (AAS)](https://opcfoundation.org/developer-tools/specifications-opc-ua-information-models/opc-ua-for-i4-asset-administration-shell/)

<p align="right">(<a href="#top">back to top</a>)</p>

## License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.

Copyright (C) 2022 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131 Karlsruhe, Germany.

You should have received a copy of the Apache 2.0 License along with this program. If not, see https://www.apache.org/licenses/LICENSE-2.0.html.

<p align="right">(<a href="#top">back to top</a>)</p>

## Contact

Michael Jacoby <br>
michael.jacoby@iosb.fraunhofer.de

Jens Mueller<br>
jens.mueller@iosb.fraunhofer.de

Klaus Schick<br>
klaus.schick@iosb.fraunhofer.de

<p align="right">(<a href="#top">back to top</a>)</p>
