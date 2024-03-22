# Release Notes

<!--start:changelog-header-->
## 1.0.0-SNAPSHOT (current development version)<!--end:changelog-header-->

:::{important}
Version 1.0 is a major update and has breaking changes to all previous versions.
When upgrading to v1.0 please make sure the AAS models and payload you use are compliant to AAS spec v3.0.
Additionally, existing asset connection configurations must be updated in the config file as the serialization format for references has changed in the specificition, e.g., `(Submodel)[IRI]http://example.org/foo,(Property)[ID_SHORT]bar` in older configurations new becomes `(Submodel)http://example.org/foo, (Property)bar`, i.e. the id type has been removed and segments are not separated only by `,` but by `, ` (comma followed by additional space).
:::


**New Features & Major Changes**
- General
	- Updated to AAS metamodel & API v3.0, i.e. older model file (compliant with v2.x, v3.xRCxx) can no longer be loaded with FA³ST Service as-is but have be converted to v3.0
	- Now requires Java 17+
	- Replaced AAS model & de-/serialization library, now using [AAS4J](https://github.com/eclipse-aas4j/aas4j) (previously [Java Model](https://github.com/admin-shell-io/java-model/) and [Java Serializer](https://github.com/admin-shell-io/java-serializer)
	- Default filename for model files changed to `model.*` (previously `aasenvironment.*`)
	- Unified way to configure certificate information ([See details](#providing-certificates-in-configuration)). Affected components: HTTP Asset Connection, OPC UA Asset Connection, HTTP Endpoint, MQTT MessageBus
	- Environment variables now use `_` instead of `.` as a separator
	- Validation - **currently completely disabled as AAS4J does not yet offer validation support**
		- More fine-grained configuration of validation via configuration file
		- Enabled validation for API calls creating or updating elements (basic validation enabled by default)
		- Renamed CLI argument `--no-modelValidation` to `--no-validation`. It now enables any validation when used (overriding validation configuration in configuration file is present)
	- Renamed CLI argument `--emptyModel` to `--empty-model`
- Endpoint
	- HTTP
		- Updated to [AAS API specification v3.0.1](https://app.swaggerhub.com/apis/Plattform_i40/Entire-API-Collection/V3.0.1)
			- HTTP no longer supported, only HTTPS
			- all URLs are now prefixed with /api/v3.0/
		- Added support for AASX serialization
		- Added support for uploading, deleting and modifying of asset thumbnails and file attachments through API
	- OPC UA Endpoint
		- Updated OPC UA Information model to AAS specification version 3.0. As there is no official mapping of AAS v3.0 to OPC UA, the current mapping is proprietary
		- Added support for configuring supported security policies (`NONE`, `BASIC128RSA15`, `BASIC256`, `BASIC256SHA256`, `AES128_SHA256_RSAOAEP`, `AES256_SHA256_RSAPSS`) and authentication methods (`Anonymous`, `UserName`, `Certificate`)
- MessageBus
	- MQTT-based MessagBus now available that supports running both as embedded MQTT server or using external one
- Asset Connection
	- HTTP
		- Now provides a way to explicitely trust server certificates, e.g. useful when servers are using a self-signed certificate
- File-storage
	- New file-storage interface provides functionality to store referenced files like thumbnails and files in SubmodelElements
	- Implementations for filesystem- and memory-based storages
    
**Internal changes & bugfixes**
- General
	- Fixed a `ConcurrentModificationException` that could occur when accessing a submodel with subscription-based asset connection via HTTP endpoint
- HTTP Endpoint
	- Now correctly uses base64URL-encoding for all HTTP requests (instead of base64-encoding for some)
	- No longer leaks sensitive server information in HTTP response headers (such as server version of the HTTP server library)
- Asset Connection
	- OPC UA
		- Unit tests no longer create temp files in source folders
- Starter
	- Improved error logging

## 0.5.0

**New Features & Major Changes**
- Improved exception handling in CLI - upon error starter application should now correctly terminate with error code 1
- OPC UA Endpoint
	- Additional parameters available in configuration
- Docker container now runs using a non-root user
- Base persistence configuration updated
	- changed `initialModel` from filename to `AASEnvironment` object
	- added  `initialModelFile`
	- removed `decoupleEnvironment` property. To achieve previous behavior you need to manually decouple the model by making a deep copy, e.g. via `DeepCopyHelper.deepCopy(...)`
- Asset Connection
	- OPC UA
		- Support mapping to specific element in (multi-dimensional) array/vector
		- Additional parameters available in configuration: requestTimeout, acknowledgeTimeout, retries, securityPolicy, securityMode, securityBaseDir, transportProfile, userTokenType, applicationCertificateFile, applicationCertificatePassword, authenticationCertificateFile, authenticationCertificatePassword

**Internal changes & bugfixes**
- General
	- Improved startup process & console ouput
- HTTP Endpoint
	- DELETE requests now correctly return HTTP status code `204 No Content`. The following URL patterns are affected:
		- /submodels/{submodelIdentifier}
		- /submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}
		- /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}
	- Using not allowed HTTP methods not correctly returns `405 Method Not Allowed` instead of `500 Internal Server Error`
	- Unsupported URLS (valid URLs with additional path elements) now correctly return `400 Bad Request` instead of `405 Method not allowed`
	- GET /shells/{aasIdentifier} now correctly returns status code `404 Not Found` when called with an existing ID that is not an AAS (instead of `500 Internal Server Error`)
- OPC UA Endpoint
	- Major code refactoring
- Persistence
	- Major code refactoring
- Asset Connection
	- Fixed endless feedback loop when adding a subscription provider and value provider to the same element
	- OPC UA
		- fixed deserialization error when using operation provider with argument mappings
	- HTTP
		- subscription provider now only fires when then value has changed (before that it fired with any read)
- Miscellaneous
	- Now using dockerfile to build docker container instead of jib maven plugin

## 0.4.0

**New Features**
- Improved logging (new CLI arguments `-q`, `-v`, `-vv`, `-vvv`, `--loglevel-faaast`, `--loglevel-external`)

**Internal changes & bugfixes**
- Asset Connection
	- OPC UA
		- Fixed problem converting DateTime values
- Fixed error related to JSONPath expressions that could occure in asset connections when using certain JSONPath expressions
- Fixed error in reference helper with setting proper type of key elements when an identifiable and a independant referable have the same idshort
- Removed dependencies on checks module which is only needed for codestyle check while compiling and therefore not released on maven. This caused a missing dependency exception when using any FA³ST module within your code.

## 0.3.0

**New Features**
- Asset Connection
	- OPC UA
		- Automatic reconnect upon connection loss
		- Add ParentNodeId to OpcUaOperationProviderConfig
		- Introduce mapping between IdShort and Argument Name in OpcUaOperationProviderConfig
	- MQTT
		- Automatic reconnect upon connection loss
	- HTTP
		- Now supports adding custom HTTP headers (on connection- & provier-level)
- Improved JavaDoc documentation
- Improved security through automatic vulnerabilities check before release
- Added example how to implement custom asset connection

**Internal changes &  bugfixes**
- Dynamic loading of custom implementations (AssetConnection, Persistence, MessageBus, Endpoint and Dataformat) now works as expected. NOTE: This requires package your custom implementation as a fat jar and put it in the same location as the FA³ST starter jar.
- Streamlining dependencies
- Improved console output for file paths
- Added checks to ensure model paths provided are valid
- Asset Connection
	- OPC UA
		- Fix problem when InputArguments or OutputArguments node was not present for Operations
		- Use ExpandedNodeId to parse NodeId Strings
	- HTTP
		- Fixed problem when using HttpAssetConnection configuration
- Development
	- Enforce JavaDoc present at compile-time (through checkstyle)
	- No longer release `test` module
	- Create javadoc jar for parent POM

## 0.2.1

**Bugfixes**
- Asset connections could not be started with OperationProvider
- Returning wrong HTTP responses in some cases

## 0.2.0

**New Features**
- Persistence
	- File-based persistence added
	- Each persistence implementation can now be configured to use a given AAS model as initial value
- Asset Connection
	- HTTP asset connection added
	- Basic authentication (username & password) added for OPC UA, MQTT and HTTP
	- Introducing protocol-agnostic library for handling different payload formats including extracting relevant information from received messages as well as template-based formatting of outgoing messages (currently only implemented for JSON)
- HTTP Endpoint
	- API
		- `Submodel Interface` calls now also available in combination with `Asset Administration Shell Interface`, e.g. /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel
		- `Asset Administration Shell Serialization Interface` now supported (at /serialization)
	- Support for output modifier `content=path`
	- CORS support, can be enabled by setting `isCorsEnabled=true` in config (default: false)
	- now returns status code 405 Method Not Allowed if URL is correct but requested method is not supported
- Support for `valueType=DateTime`
- Support for Java 16
- Improved robustness (e.g. against common invalid user input or network issues)
- Improved console output (less verbose, always displays version info)
- Improved documentation

**Internal changes & smaller bugfixes**
- Validation now checks for unsupported datatypes
- Version info correctly displayed when started as docker container or via local build/debug
- Fixed potential crash when initializing value with empty string althtough that is not a valid value according to the value type, e.g. int, double, etc. (empty string value is treated the same as null)
- Asset Connection
	- Fixed error when using operation provider
	- OPC UA
		- subscription provider now syncs value upon initial connect instead of waiting for first value change on server
	- MQTT
		- print warning upon connection loss
		- properly handle invalid messages without crashing
- Added strict enforcement of valid output modifiers for each API call
- Dynamically allocate ports in unit tests
- Add builder classes for event messages & config classes
- Replace AASEnvironmentHelper with methods of EnvironmentSerialization

## 0.1.0

First release!
