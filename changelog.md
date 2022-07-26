# Changelog

## Current development version (0.2.0-SNAPSHOT)

**New Features**
*   Path to AAS Environment model can be set in persistence config
*   Add basic for basic username & password authentication for asset connections (MQTT, OPC UA, HTTP)
*   validation now checks for unsupported datatypes

**Internal changes & Bugfixes**
*   fixed potential crash when initializing value with empty string althtough that is not a valid value according to the value type, e.g. int, double, etc. (empty string value is treated the same as null)

## Release version 0.1.0

First release!
