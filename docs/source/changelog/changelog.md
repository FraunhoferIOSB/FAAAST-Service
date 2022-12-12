# Changelog

<!--start:changelog-header-->
## Current development version (0.4.0-SNAPSHOT)<!--end:changelog-header-->

**New Features**
*   Improved logging (new CLI arguments `-q`, `-v`, `-vv`, `-vvv`, `--loglevel-faaast`, `--loglevel-external`)

**Internal changes & bugfixes**
*   Asset Connection
	*   OPC UA
		*   Fixed problem converting DateTime values
*   Fixed error related to JSONPath expressions that could occure in asset connections when using certain JSONPath expressions
*   Fixed error in reference helper with setting proper type of key elements when an identifiable and a independant referable have the same idshort
*   Removed dependencies on checks module which is only needed for codestyle check while compiling and therefore not released on maven. This caused a missing dependency exception when using any FA³ST module within your code.

## Release version 0.3.0

**New Features**
*   Asset Connection
	*   OPC UA
		*   Automatic reconnect upon connection loss
		*   Add ParentNodeId to OpcUaOperationProviderConfig
		*   Introduce mapping between IdShort and Argument Name in OpcUaOperationProviderConfig
	*   MQTT
		*   Automatic reconnect upon connection loss
	*   HTTP
		*   Now supports adding custom HTTP headers (on connection- & provier-level)
*   Improved JavaDoc documentation
*   Improved security through automatic vulnerabilities check before release
*   Added example how to implement custom asset connection

**Internal changes &  bugfixes**
*   Dynamic loading of custom implementations (AssetConnection, Persistence, MessageBus, Endpoint and Dataformat) now works as expected. NOTE: This requires package your custom implementation as a fat jar and put it in the same location as the FA³ST starter jar.
*   Streamlining dependencies
*   Improved console output for file paths
*   Added checks to ensure model paths provided are valid
*   Asset Connection
	*   OPC UA
		*   Fix problem when InputArguments or OutputArguments node was not present for Operations
		*   Use ExpandedNodeId to parse NodeId Strings
	*   HTTP
		*   Fixed problem when using HttpAssetConnection configuration
*   Development
	*   Enforce JavaDoc present at compile-time (through checkstyle)
	*   No longer release `test` module
	*   Create javadoc jar for parent POM

## Release version 0.2.1

**Bugfixes**
*   Asset connections could not be started with OperationProvider

*   Returning wrong HTTP responses in some cases

## Release version 0.2.0

**New Features**
*   Persistence
	*   File-based persistence added
	*   Each persistence implementation can now be configured to use a given AAS model as initial value

*   Asset Connection
	*   HTTP asset connection added
	*   Basic authentication (username & password) added for OPC UA, MQTT and HTTP
	*   Introducing protocol-agnostic library for handling different payload formats including extracting relevant information from received messages as well as template-based formatting of outgoing messages (currently only implemented for JSON)

*   HTTP Endpoint
	*   API
		*   `Submodel Interface` calls now also available in combination with `Asset Administration Shell Interface`, e.g. /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel
		*   `Asset Administration Shell Serialization Interface` now supported (at /serialization)
	*   Support for output modifier `content=path`
	*   CORS support, can be enabled by setting `isCorsEnabled=true` in config (default: false)
	*   now returns status code 405 Method Not Allowed if URL is correct but requested method is not supported

*   Support for `valueType=DateTime`

*   Support for Java 16

*   Improved robustness (e.g. against common invalid user input or network issues)

*   Improved console output (less verbose, always displays version info)

*   Improved documentation

**Internal changes & smaller bugfixes**
*   Validation now checks for unsupported datatypes

*   Version info correctly displayed when started as docker container or via local build/debug

*   Fixed potential crash when initializing value with empty string althtough that is not a valid value according to the value type, e.g. int, double, etc. (empty string value is treated the same as null)

*   Asset Connection
	*   Fixed error when using operation provider
	*   OPC UA
		*   subscription provider now syncs value upon initial connect instead of waiting for first value change on server
	*   MQTT
		*   print warning upon connection loss
		*   properly handle invalid messages without crashing

*   Added strict enforcement of valid output modifiers for each API call

*   Dynamically allocate ports in unit tests

*   Add builder classes for event messages & config classes

*   Replace AASEnvironmentHelper with methods of EnvironmentSerialization 

## Release version 0.1.0

First release!
