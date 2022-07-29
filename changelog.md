# Changelog

## Current development version (0.2.0-SNAPSHOT)

**New Features**
*   Persistence
	*   File-based persistence added
	*   Each persistence implementation can now be configured to use a given AAS model as initial value

*   Asset Connection
	*   HTTP asset connection added
	*   Basic authentication (username & password) added for OPC UA, MQTT and HTTP
	*   Introducing protocol-agnostic library for handling different payload formats including extracting relevant information from received messages as well as template-based formatting of outgoing messages (currently only implemented for JSON)

*   HTTP Endpoint
	*   Support for output modifier `content=path`
	*   CORS support, can be enabled by setting `isCorsEnabled=true` in config (default: false)

*   Support for `valueType=DateTime`

*   Support for Java 16

*   Improved robustness (e.g. against common invalid user input or network issues)

*   Improved console output (less verbose, always displays version info)

**Internal changes & Bugfixes**
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

## Release version 0.1.0

First release!
