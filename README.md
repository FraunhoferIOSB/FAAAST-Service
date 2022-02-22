
# FA³ST Service

<p>
  <a target="_blank">
    <img src="./documentation/images/Fa3st-Service_positiv.png" alt="FA³ST Service Logo" width="350" >
  </a>
  <a target="_blank">
    <img src="./documentation/images/Fa3st-Service_negativ.png" alt="FA³ST Service Logo" width="350">
  </a>
</p>

FA³ST Service implements the [Asset Administration Shell (AAS) specification from the platform Industrie 4.0](https://www.plattform-i40.de/SiteGlobals/IP/Forms/Listen/Downloads/EN/Downloads_Formular.html?cl2Categories_TechnologieAnwendungsbereich_name=Verwaltungsschale) and built up an easy-to-use Web Service based on a custom AAS model instance.

| Currently we publish the FA³ST Service here as a <mark>BETA Version</mark> since there are not all functionalities fully tested. However, contributions like bug issues or questions are highly welcome. |
|-----------------------------|
| |

<!-- GETTING STARTED -->
## Getting Started

This is an example of how you may setting up your project locally.
To get a local copy up and running follow these simple example steps.

### Prerequisites

* maven
* java 11

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

## Roadmap

We will continously expand the features of the FA³ST Service. However, we highly welcome bug reports, feature requests, code contributions, and assistance with testing. Our next steps and integrations will be:
- Implement a file & database persistence 
- Expand the functionality and configuration options in the MQTT/OPCUA AssetConnections
- FA³ST Registry

## Usage with Command Line

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
- json
- json-ld
- aml
- xml
- opcua nodeset
- rdf

<hr>
<p>

Following command line parameters could be used:
```
-c, --configFile=<configFilePath>
						The config file path. Default Value = config.json
-e, --environmentFile=<aasEnvironmentFilePath>
						Asset Administration Shell Environment FilePath.
							Default Value = aasenvironment.*
	--emptyEnvironment   Starts the FA³ST service with an empty Asset
							Administration Shell Environment. False by default
	--endpoints[=<endpoints>...]

-h, --help               Show this help message and exit.
	--[no-]autoCompleteConfig
						Autocompletes the configuration with default values
							for required configuration sections. True by
							default
	--[no-]modelValidation
						Validates the AAS Environment. True by default
-V, --version            Print version information and exit.
```
<hr>
<p>

#### Change the Configuration
<p>

In general there are 3 ways to configure your FA³ST Service:
1. Default values
2. Commandline parameters
3. Environment Variables

The 3 kinds can be combined, e.g. by using the default configuration and customizing with commandline parameters and environment variables. If they conflict, environment variables are preferred over all and commandline parameters are preferred over the default values.

Without any manual customization a FA³ST Service with default configuration will be started. For details to the structure and components of the configuration please have a look at the configuration section [here]()

Default Configuration:
```json
{
	"core" : {
		"requestHandlerThreadPoolSize" : 2
	},
	"endpoints" : [ {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint"
	} ],
	"persistence" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemory"
	},
	"messageBus" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternal"
	}
}
```
<hr>
<p>


The FA³ST Service Starter consider following environment variables:
- `faaast.configFilePath` to use a own configuration file
- `faaast.aasEnvFilePath` to use a Asset Administration Environment file

Environment variables could also be used to adjust some config components in the configuration:
- `faaast.configParameter.[dot.separated.path]`

If you want to change for example the requestHandlerThreadPoolSize in the core configuration, just set the environment variable `faaast.configParameter.core.requestHandlerThreadPoolSize=42`. To access configuration components in a list use the index. For example to change the port of the HTTP endpoint in the default configuration you can set the environment variable `faaast.configParameter.endpoints.0.port=8081`.

<hr>
<p>

You could also use properties to adjust configuration components with the `-D` parameter. To change the `requestHandlerThreadPoolSize` of the core component and the port of the http endpoint use
```sh
java -jar starter-{version}.jar -e {path/to/your/AASEnvironment}
-Dcore.requestHandlerThreadPoolSize=42 -Dendpoints.0.port=8081
```
<hr>
<p>

#### Special Parameters

The parameter `--endpoints` accepts a list of endpoints which should be started with the service. Currently supported is only `http`. So a execution of
```sh
java -jar starter-{version}.jar -e {path/to/your/AASEnvironment} --endoints http
```
leads to a FA³ST Service with the HTTP endpoint implemented in class `de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint`.

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- FEATURES -->
### Features
<!-- HTTP-ENDPOINT -->
#### HTTP-Endpoint Interface

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

<!-- HTTP-EXAMPLE -->
##### HTTP Example
Sample HTTP-Call for Operation _GetSubmodelElementByPath_
using the parameters
- _submodelIdentifier_: https://acplt.org/Test_Submodel (must be base64URL-encoded)
- _idShortPath_: ExampleRelationshipElement (must be URL-encoded)

using the query-parameters _level=deep_ and _content=normal_.

> To avoid problems with IRIs in URLs the identifiers shall be BASE64-URL-encoded before using them as
parameters in the HTTP-APIs. IdshortPaths are URL-encoded to handle including square brackets.

```sh
http://localhost:8080/submodels/aHR0cHM6Ly9hY3BsdC5vcmcvVGVzdF9TdWJtb2RlbA==/submodel/submodel-elements/ExampleRelationshipElement?level=deep&content=normal
```

Returns a specific submodel element from the Submodel at a specified path
<p align="right">(<a href="#top">back to top</a>)</p>
<hr>

#### HTTP-API
##### The following interface URLs are fully supported:
* Asset Administration Shell Repository Interface
	* /shells (GET, POST)
	* /shells/{aasIdentifier} (GET, PUT, DELETE)
* Asset Administration Shell Interface
	* /shells/{aasIdentifier}/aas (GET, PUT)
	* /shells/{aasIdentifier}/aas/asset-information (GET, PUT)
	* /shells/{aasIdentifier}/aas/submodels (GET,POST)
	* /shells/{aasIdentifier}/aas/submodels{submodeIdentifier} (DELETE)
* Submodel Repository Interface
	* /submodels (GET, POST)
	* /submodels/{submodelIdentifier} (GET, PUT, DELETE)
* Submodel Interface
	* /submodels/{submodelIdentifier}/submodel (GET, PUT)
	* /submodels/{submodelIdentifier}/submodel/submodel-elements (POST)
	* /submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath} (GET, POST, PUT, DELETE)
	* /submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/invoke (POST)
	* /submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/operation-Results/{handle-Id} (GET)
* Concept Description Repository Interface
	* concept-descriptions (GET, POST)
	* concept-descriptions/{cdIdentifier} (GET, PUT, DELETE)

###### Optional query params are
* level=deep/core
* content=normal/trimmed/value
* extent=WithoutBLOBValue/WithBLOBValue
* InvokeOperation supports async=true/false

They are added to the URL as regular query params
```sh
http://url:port?level=deep&content=value
```
FA³ST Service currently supports only content=value and content=normal

<p align="right">(<a href="#top">back to top</a>)</p>

##### The following interface URLs are currently not (yet) supported:
* Submodel Repository Interface (Alternative Interface URLs "Swagger Doc Feb. 2022",
[DotAAS Part 2 | HTTP/REST | Asset Administration Shell Repository](https://app.swaggerhub.com/apis/Plattform_i40/AssetAdministrationShell-Repository/Final-Draft#/Asset%20Administration%20Shell%20Repository/GetSubmodel) (yet not fully specified))
	* /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel
	* /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements
	* /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}
	* /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/invoke
	* /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/operation-results/{handleId}
* Asset Administration Shell Registry Interface (not in Scope of FA³ST-Service)
* Submodel Registry Interface (not in Scope of FA³ST-Service)


* AASX File Server Interface (not yet supported)
	* /packages
	* /packages/{packageId}
* Asset Administration Shell Serialization Interface (not yet supported)
	* /serialization (GET)
* Asset Administration Shell Basic Discovery (not yet supported)
	* /lookup/shells
	* /lookup/shells/{aasIdentifier}

<p align="right">(<a href="#top">back to top</a>)</p>
<hr>

<!-- OPC UA ENDPOINT -->
#### OPC UA Endpoint Interface
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

##### Supported Functions
* Operations (OPC UA method calls). Exception: Inoutput-Variables are not supported in OPC UA.
* Write Values
	* Property
	* Value
	* Range
	* Min
		* Max
	* Blob
	* Value
	* MultiLanguageProperty
	* Value
	* ReferenceElement
	* Value
	* RelationshipElement
	* First
		* Second
	* Entity
	* GlobalAssetID
		* Type

##### Not (yet) Supported Functions
* Events (not yet supported)
* Write Values (not yet supported)
	* DataSpecifications
	* Qualifier
	* Category
	* ModelingKind
* AASValueTypeDataType (not yet supported)
	* ByteString
	* Byte
	* UInt16
	* UInt32
	* UInt64
	* DateTime
	* LocalizedText
	* UtcTime

<p align="right">(<a href="#top">back to top</a>)</p>
<hr>

<p align="right">(<a href="#top">back to top</a>)</p>

## Persistence
### `InMemory`
#### Yet not implemented
- AASX Packages
- Package Descriptors
- OutputModifier Content
- SubmodelElementStructs

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- HOW TO DEVELOP -->
## Develop
### Spotless
The project uses *spotless:check* in the build cycle, which means the project only compiles if all code, *.pom and *.xml files are formatted according to the project's codestyle definitions (see details on [spotless](https://github.com/diffplug/spotless)).
You can automatically format your code by running

> mvn spotless:apply

Additionally, you can import the eclipse formatting rules defined in */codestyle* into our IDE.

<p align="right">(<a href="#top">back to top</a>)</p>


### Third Party License Usage Report
Generates a report of the licenses used in the subsystems (dependencies).
The report is stored in the directory ./documentation/third_party_licenses_report as a html page.
> mvn license:aggregate-third-party-report


<!-- ROADMAP -->
## Known Issues

See the [open issues](https://github.com/FraunhoferIOSB/FAAAST-Service/issues) for a full list of proposed features (and known issues).

<p align="right">(<a href="#top">back to top</a>)</p>


<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- FA³ST Service General Intro -->

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

<!-- LICENSE -->
## License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.

Copyright (C) 2022 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131 Karlsruhe, Germany.

You should have received a copy of the Apache 2.0 License along with this program. If not, see https://www.apache.org/licenses/LICENSE-2.0.html.

<p align="right">(<a href="#top">back to top</a>)</p>


<!-- CONTACT -->
## Contact

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- ACKNOWLEDGMENTS -->
## Acknowledgments

* []()
* []()
* []()

<p align="right">(<a href="#top">back to top</a>)</p>
