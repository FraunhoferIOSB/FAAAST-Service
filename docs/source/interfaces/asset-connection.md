# Asset Connection
`AssetConnection` implementations allow connecting/synchronizing elements of your AAS to/with assets via different protocol. This functionality is further divided into  3 so-called provider, namely
-   [ValueProvider](https://github.com/FraunhoferIOSB/FAAAST-Service/blob/main/core/src/main/java/de/fraunhofer/iosb/ilt/faaast/service/assetconnection/AssetValueProvider.java): supports reading and writing values from/to the asset, i.e. each time a value is read or written via an endpoint the request is forwarded to the asset
-   [OperationProvider](https://github.com/FraunhoferIOSB/FAAAST-Service/blob/main/core/src/main/java/de/fraunhofer/iosb/ilt/faaast/service/assetconnection/AssetOperationProvider.java): supports the execution of operations, i.e. forwards operation invocation requests to the asset and returning the result value,
-   [SubscriptionProvider](https://github.com/FraunhoferIOSB/FAAAST-Service/blob/main/core/src/main/java/de/fraunhofer/iosb/ilt/faaast/service/assetconnection/AssetSubscriptionProvider.java): supports synchronizing the AAS with pub/sub-based assets, i.e. subscribes to the assets and updates the AAS with new values over time.

An implementation does not have to implement all providers, in fact it is often not possible to implement all of them for a given network protocol as most protocols do not support pull-based and pub/sub mechanisms at the same time (e.g. HTTP, MQTT).

Each provider is connected to exactly one element of the AAS. Each asset connection can have multiples of each provider type. Each FA³ST Service can have multiple asset connections.
Accordingly, each asset connection configuration supports at least this minimum structure

```json
{
	"@class": "...",
	"valueProviders":
	{
		"{serialized Reference of AAS element}":
		{
			// value provider configuration
		}
	},
	"operationProviders":
	{
		"{serialized Reference of AAS element}":
		{
			// operation provider configuration
		}
	},
	"subscriptionProviders":
	{
		"{serialized Reference of AAS element}":
		{
			// subscription provider configuration
		}
	}
}
```

A concrete example for OPC UA asset connection could look like this
```json

{
	"@class": "de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.OpcUaAssetConnection",
	"host": "opc.tcp://localhost:4840",
	"initializationInterval" : 10000,
	"valueProviders":
	{
		"(Submodel)[IRI]urn:aas:id:example:submodel:1,(Property)[ID_SHORT]Property1":
		{
			"nodeId": "some.node.id.property.1"
		},
		"(Submodel)[IRI]urn:aas:id:example:submodel:1,(Property)[ID_SHORT]Property2":
		{
			"nodeId": "some.node.id.property.2"
		}
	},
	"operationProviders":
	{
		"(Submodel)[IRI]urn:aas:id:example:submodel:1,(Operation)[ID_SHORT]Operation1":
		{
			"nodeId": "some.node.id.operation.1"
		}
	},
	"subscriptionProviders":
	{
		"(Submodel)[IRI]urn:aas:id:example:submodel:1,(Property)[ID_SHORT]Property3":
		{
			"nodeId": "some.node.id.property.3",
			"interval": 1000
		}
	}
}
```

Initialization of the asset connection occurs asynchronously during startup and is established as soon as the asset can be reached. This functionality allows the FA³ST service to start before the assets do. The `initializationInterval` attribute can be used to set the retry time in milliseconds. The default value is `10000`.

```json
{
	"@class": "...",
	"initializationInterval" : 10000,
	"valueProviders":{ }
}
```

## HTTP

### Supported Providers

-   ValueProvider
    -   read ✔️
	-   write ✔️
-   OperationProvider ✔️
-   SubscriptionProvider ✔️ (via polling)

### Configuration Parameters

#### Asset Connection

| Name | Allowed Value | Description                                                                                                                                                                                                                                                                                            |
|:--| -- |--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| baseUrl | String | Base URL of the HTTP server, e.g. _http://example.com_                                                                                                                                                                                                                                                 |
| username | String | _optional_ username for connecting to the HTTP server                                                                                                                                                                                                                                                  |
| password | String | _optional_ password for connecting to the HTTP server                                                                                                                                                                                                                                                  |
| headers | Map<String,String> | _optional_ headers to send with each request                                                                                                                                                                                                                                                           |
| trustedCertificates | Object | _optional_  information to load a key store containing certificates that should be trusted, i.e. when connecting to a server that is using self-signed certificates that by default would not be trusted. [See details](../../gettingstarted/configuration#providing-certificates-in-configuration)     |              |   


#### Value Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| format | JSON\|XML | content format of payload |
| path | String | Path for the HTTP request, relative to the `baseUrl` of the connection |
| headers | Map<String,String> | _optional_ headers to send with each request (overrides connection-level headers) |
| query | String | _optional_ additional information how to extract actual value from received messages, depends on `format`, e.g. for JSON this is a JSON Path expression |
| template | String | _optional_ template used to format payload when sending via HTTP |
| writeMethod | GET\|PUT\|POST | _optional_ HTTP method to use when writing a value to HTTP, default: PUT |

##### Example

```json
{
	"format": "JSON",
	"path": "/foo",
	"headers": {
		"foo": "bar"
	},
	"query": "$.foo",
	"template": "{\"foo\" : \"${value}\"}",
	"writeMethod": "POST"
}
```

#### Operation Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| format | JSON\|XML | content format of payload |
| path | String | Path for the HTTP request, relative to the `baseUrl` of the connection |
| headers | Map<String,String> | _optional_ headers to send with each request (overrides connection-level headers) |
| method | PUT\|POST | _optional_ HTTP method to use, default: POST |
| template | String | _optional_ template used to format payload when sending via HTTP |
| queries | Map<String, String> | _optional_ Map of result variable idShorts and corresponding query expressions to fetch them from returned value, query expressions depend on `format`, e.g for JSON these are JSON Path expressions |


##### Example

Operation with input parameters `in1` and `in2` and output parameters `out1` and `out2`

```json
{
	"format": "JSON",
	"path": "/foo/execute",
	"headers": {
		"foo": "bar"
	},
	"method": "POST",	
	"template": "{\"input1\" : \"${in1}\", \"input2\" : \"${in2}\"}",
	"queries": {
		"out1": "$.output1",
		"out2": "$.output2"
	}
}
```

#### Subscription Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| format | JSON\|XML | content format of payload |
| path | String | Path for the HTTP request, relative to the `baseUrl` of the connection |
| headers | Map<String,String> | _optional_ headers to send with each request (overrides connection-level headers) |
| interval | long | _optional_ Interval to poll the server for changes (in ms), default: 100 |
| method | GET\|PUT\|POST | _optional_ HTTP method to use, default: GET |
| payload | String | _optional_ Static content to send which each request |
| query | String | _optional_ additional information how to extract actual value from received messages, depends on `format`, e.g. for JSON this is a JSON Path expression |


##### Example

```json
{
	"path": "/foo",
	"headers": {
		"foo": "bar"
	},
	"interval": "500",
	"method": "GET",
	"template": "{\"foo\" : \"bar\"}"
}
```

## MQTT

### Supported Providers

-   ValueProvider
    -   read ❌
	-   write ✔️
-   OperationProvider ❌
-   SubscriptionProvider ✔️

### Configuration Parameters

#### Asset Connection

| Name | Allowed Value | Description |
|:--| -- | -- |
| serverUri | String | URL of the MQTT server, e.g. _tcp://localhost:1883_ |
| clientId | String | _optional_ Id of the MQTT client used to connect to the server, default: random value |
| username | String | _optional_ Username for connecting to the MQTT server |
| password | String | _optional_ Password for connecting to the MQTT server |

#### Value Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| format | JSON\|XML | Content format of payload |
| topic | String | MQTT topic to use |
| template | String | _optional_ Template used to format payload |

##### Example

```json
{
	"format": "JSON",
	"topic": "example/myTopic",
	"template": "{\"foo\" : \"${value}\"}"
}
```

#### Subscription Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| format | JSON\|XML | Content format of payload |
| topic | String | MQTT topic to use |
| query | String | _optional_ Additional information how to extract actual value from received messages, depends on `format`, e.g. for JSON this is a JSON Path expression. |

##### Example

```json
{
	"format": "JSON",
	"topic": "example/myTopic",
	"query": "$.foo"
}
```

## OPC UA

### Supported Providers

-   ValueProvider
    -   read ✔️
	-   write ✔️
-   OperationProvider ✔️
-   SubscriptionProvider ✔️

### Configuration Parameters

#### Asset Connection

| Name | Allowed Value | Description |
|:--| -- | -- |
| host | String | URL of the OPC UA server, e.g. _opc.tcp://localhost:4840_ |
| userTokenType | Enum | _optional_ User Token Type for connecting to the OPC UA server. Possible values are: Anonymous, UserName, Certificate. Default value is Anonymous |
| username | String | _optional_ Username for connecting to the OPC UA server. This value is required if userTokenType UserName is selected. |
| password | String | _optional_ Password for connecting to the OPC UA server. This value is required if userTokenType UserName is selected. |
| requestTimeout | int | _optional_ Timeout for requests (in ms), default: 3000 |
| acknowledgeTimeout | int | _optional_ Timeout for acknowledgement (in ms), default: 10000 |
| securityPolicy | Enum | _optional_ Desired Security Policy for the connection to the OPC UA server. Possible values are: None, Basic256Sha256, Aes128_Sha256_RsaOaep and Aes256_Sha256_RsaPss. Default value is None. |
| securityMode | Enum | _optional_ Security Mode for the connection to the OPC UA server. Possible values are: None, Sign and SignAndEncrypt. Default value is None. |
| transportProfile | Enum | _optional_ Transport Profile for the connection to the OPC UA server. Possible values are: TCP_UASC_UABINARY, HTTPS_UABINARY, HTTPS_UAXML, HTTPS_UAJSON, WSS_UASC_UABINARY, WSS_UAJSON. Default value is TCP_UASC_UABINARY |
| securityBaseDir | String | _optional_ Base directory for the certificate handling. Default value is the current directory ("."). |
| applicationCertificate | Object | _optional_  The application certificate [See details](../../gettingstarted/configuration#providing-certificates-in-configuration) |
| authenticationCertificate | Object | _optional_  The authentication certificate [See details](../../gettingstarted/configuration#providing-certificates-in-configuration) |

##### Remarks on certificate management
In OPC UA , certificates can be used for two purposes:
- encryption & signing of messages, and
- authentication of a client.

We call the certificate used of encryption _application certificate_ and the one used for authenticating a client _authentication certificate_.
You can choose to use only one of these options or both.
If using both, you can use different or the same certificates.

##### Application Certificate
An application certificate is required if the property `securityMode` is set to `Sign` or `SignAndEncrypt`.

Which application certificate to use is determined by the following steps:
- `applicationCertificate.keyStorePath` if it is an absolute file path and the file exists (default: application.p12)
- `{securityBaseDir}/{applicationCertificate.keyStorePath}` if the file exists (default: `./{applicationCertificate.keyStorePath}`)
- otherwise generate self-signed certificate and store it at `applicationCertificate.keyStorePath` (if `applicationCertificate.keyStorePath` is an absolute file path) or else `{securityBaseDir}/{applicationCertificate.keyStorePath}`. The generated keystore will not be password protected.

You also need to make sure that the OPC UA client (which in this case is the FA³ST Service OPC UA asset connection) knows and trusts the server certificate and vice versa.

For the client to trust the server you need to do one of these steps depending on the certificate of the server:
- Self-signed-certificate: Put server certificate in {securityBaseDir}/pki/trusted/certs
- CA Certificate: put the CA root certificate in {securityBaseDir}/pki/issuers/certs and the corresponding certificate revocation list (CRL) in {securityBaseDir}/pki/issuers/crl.

If you don't have the server certificate at hand you can start FA³ST Service without providing/trusting the server certificate.
On start-up FA³ST Service will try to connect to the server which will fail because the server certificate is not trusted yet.
After that you will find the relevant files at `{securityBaseDir}/pki/rejected`.
Copy them to the respective directories as described above.
Once FA³ST Service tries to reconnect the connection should be established successfully.

For the server to trust your client application certificate please refer to the documentation of your OPC UA server.

##### Authentication Certificate
Which authentication certificate is used is determined by a similar logic as for the application certificate besides that this certificate is not auto-generated if not present:
- `authenticationCertificate.keyStorePath` if it is an absolute file path and the file exists (default: application.p12)
- `{securityBaseDir}/{authenticationCertificate.keyStorePath}` if the file exists (default: `./{authenticationCertificate.keyStorePath}`)


#### Value Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| nodeId | String | NodeId of the the OPC UA node to read/write in ExpandedNodeId format |
| arrayIndex | String | _optional_ Index of the desired array element if the value is an array |

All NodeIds (also below) are specified in the ExpandedNodeId format (see [OPC UA Reference, Part 6](https://reference.opcfoundation.org/v104/Core/docs/Part6/5.3.1/), Section ExpandedNodeId). In the following you can see two examples.

If the value is an array, it’s possible to reference a specific element of the array. The index of the desired element is specified with square brackets, e.g. “[2]”. If the value is a multidimensional array, multiple indices can be specified, e.g. "&#091;1&#093;&#091;3&#093;".

##### Example

```json
{
	"nodeId": "nsu=com:example;s=foo",
	"arrayIndex" : "[2]"
}
```

or

```json
{
	"nodeId": "ns=2;s=foo",
	"arrayIndex" : "[2]"
}
```

#### Operation Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| nodeId | String | nodeId of the OPC UA method to call in ExpandedNodeId format |
| parentNodeId | String | _optional_ nodeId of the OPC UA object in ExpandedNodeId format, in which the method is contained. When no parentNodeId is given here, the parent object of the method is used |
| inputArgumentMapping | List&lt;ArgumentMapping&gt; | _optional_ list of mappings for input arguments between the idShort of a SubmodelElement and an argument name |
| outputArgumentMapping | List&lt;ArgumentMapping&gt; | _optional_ list of mappings for output arguments between the idShort of a SubmodelElement and an argument name |

##### Example

```json
{
	"nodeId": "nsu=com:example;s=foo",
	"parentNodeId": "nsu=com:example;s=fooObject",
	"inputArgumentMapping": 
	[
		{
			"idShort": "ExampleInputId",
			"argumentName": "ExampleInput"
		}
	],
	"outputArgumentMapping": 
	[
		{
			"idShort": "ExampleOutputId",
			"argumentName": "ExampleOutput"
		}
	]
}
```

#### Subscription Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| nodeId | String | NodeId of the the OPC UA node to read/write in ExpandedNodeId format |
| interval | long | Interval to poll the server for changes (in ms), default: 1000, _currently not used_ |
| arrayIndex | String | _optional_ Index of the desired array element if the value is an array |

If the value is an array, it's possible to reference a specific element of the array. The index of the desired element is specified with square brackets, e.g. "[2]".  If the value is multidimensional array, multiple indices can be specified, e.g. "&#091;1&#093;&#091;3&#093;".

##### Example

```json
{
	"nodeId": "nsu=com:example;s=foo",
	"interval": 1000,
	"arrayIndex" : "[2]"
}
```

### Complete Example

A complete example for OPC UA asset connection could look like this

```json

{
	"@class": "de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.OpcUaAssetConnection",
	"host": "opc.tcp://localhost:4840",
	"securityPolicy": "None",
	"securityMode" : "None",
	"applicationCertificate": {
		"keyStoreType": "PKCS12",
		"keyStorePath": "C:\faaast\MyKeyStore.p12",
		"keyStorePassword": "changeit",
		"keyAlias": "app-cert",
		"keyPassword": "changeit"
	},
	"authenticationCertificate": {
		"keyStoreType": "PKCS12",
		"keyStorePath": "C:\faaast\MyKeyStore.p12",
		"keyStorePassword": "changeit",
		"keyAlias": "auth-cert",
		"keyPassword": "changeit"
	},
	"valueProviders":
	{
		"(Submodel)[IRI]urn:aas:id:example:submodel:1,(Property)[ID_SHORT]Property1":
		{
			"nodeId": "some.node.id.property.1"
		},
		"(Submodel)[IRI]urn:aas:id:example:submodel:1,(Property)[ID_SHORT]Property2":
		{
			"nodeId": "some.node.id.property.2"
		}
	},
	"operationProviders":
	{
		"(Submodel)[IRI]urn:aas:id:example:submodel:1,(Operation)[ID_SHORT]Operation1":
		{
			"nodeId": "some.node.id.operation.1"
		}
	},
	"subscriptionProviders":
	{
		"(Submodel)[IRI]urn:aas:id:example:submodel:1,(Property)[ID_SHORT]Property3":
		{
			"nodeId": "some.node.id.property.3",
			"interval": 1000
		}
	}
}
```

