# Endpoint

The `Endpoint` interface is responsible for communication with the AAS from the outside, e.g. users or external applications. 
An instance of FA³ST Service can serve multiple endpoints at the same time.
Endpoints will be synchronized, meaning if a FA³ST Service offers multiple endpoint such as HTTP(S) and OPC UA at the same time, changes done via one of the endpoints like updating a value is reflected in the other.

The following is an example of the relevant part of the configuration part comprising both an HTTP(S) and OPC UA endpoint

```{code-block} json
:caption: Example configuration for running both an HTTP and OPC UA endpoint.
:lineno-start: 1
{
	"endpoints": [
		{
			"@class": "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint",
			"port": 443,
			"corsEnabled": true
		},
		{
			"@class": "de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.OpcUaEndpoint",
			"tcpPort": 8081
		}
	],
	// ...
}
```
(endpoint-http)=
## HTTP


The HTTP Endpoint allows accessing data and execute operations within the FA³ST Service via REST-API.
In accordance to the specification, only HTTPS is supported since AAS v3.0. 
The HTTP Endpoint is based on the document [Details of the Asset Administration Shell - Part 2: Application Programming Interfaces v3.0](https://industrialdigitaltwin.org/en/content-hub/aasspecifications/specification-of-the-asset-administration-shell-part-1-metamodel-idta-number-01001-3-0) and the corresponding [OpenAPI documentation v3.0.1](https://app.swaggerhub.com/apis/Plattform_i40/Entire-API-Collection/V3.0.1).

### Configuration

:::{table} Configuration properties of HTTP Endpoint.
| Name                        | Allowed Value                                               | Description                                                                                                                                                                              | Default Value                      |
| --------------------------- | ----------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------- |
| certificate<br>*(optional)* | [CertificateInfo](#providing-certificates-in-configuration) | The HTTPS certificate to use.<br>                                                                                                                                                        | self-signed certificate            |
| corsEnabled<br>*(optional)* | Boolean                                                     | If Cross-Origin Resource Sharing (CORS) should be enabled.<br>Typically required if you want to access the REST interface from any machine other than the one running FA³ST Service.    | false                              |
| hostname<br>*(optional)*    | String                                                      | The hostname to be used for automatic registration with registry.                                                                                                                        | auto-detect (typically IP address) |
| port<br>*(optional)*        | Integer                                                     | The port to use.                                                                                                                                                                         | 443                                |
| sniEnabled<br>*(optional)*  | Boolean                                                     | If Server Name Identification (SNI) should be enabled.<br>**This should only be disabled for testing purposes as it may present a security risk!**                                       | true                               |
| sslEnabled<br>*(optional)*  | Boolean                                                     | If SSL/HTTPS should be enabled.<br>**This should only be disabled for testing purposes as it may present a security risk!**                                                              | true                               |
:::

```{code-block} json
:caption: Example configuration section for HTTP Endpoint.
:lineno-start: 1
{
	"endpoints": [ {
		"@class": "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint",
		"certificate": {
			"keyStoreType": "PKCS12",
			"keyStorePath": "C:\faaast\MyKeyStore.p12",
			"keyStorePassword": "changeit",
			"keyAlias": "server-key",
			"keyPassword": "changeit"
		},
		"corsEnabled": true,
		"hostname": "localhost,
		"port": 443,
		"sniEnabled": true,
		"sslEnabled": true
	} ],
	// ...
}
```

### API

FA³ST Service supports the following APIs as defined by the [OpenAPI documentation v3.0.1](https://app.swaggerhub.com/apis/Plattform_i40/Entire-API-Collection/V3.0.1)

- Asset Administration Shell API
- Submodel API
- Asset Administration Shell Repository API
- Submodel Repository API
- Concept Description API
- Asset Administration Shell Basic Discovery API
- Serialization API
- Description API

#### Using HTTP PATCH

As the AAS specification is currently does not properly specify show HTTP PATCH requests are expected to work, FA³ST Service follows the well-established [RFC 7386 JSON Merge Patch](https://datatracker.ietf.org/doc/html/rfc7386).
In short, this means that as payload you can send a JSON document that only contains the properties of the original document you want to update.
To delete elements set the value explicitely to null.

As a consequence, URLs for all different content modifiers, i.e. `/$metadata`, `/$value`, as well as the call without any modifiers, are redundant and provide exactly the same functionality in FA³ST Service.

:::{caution}
Arrays in JSON objects can only be replaced, i.e. if you want to update a single element within an array you first need to get the current value of the array, modify the element to be updated and then send the whole array as part of the PATCH payload.
:::

#### Invoking Operations

To invoke an operation, make a `POST` request according to this URL example: `/submodels/{submodelId (base64-URL-encoded)}/submodel-elements/{idShortPath to operation}/invoke`.

:::{tip}
You can invoke operations asynchronuously by calling `.../invoke-async` instead of `.../invoke` in which case you get back a `handleId` instead of the result.
To monitor the execution state call `.../operation-status/{handleId}` and once finished you can get the result calling `.../operation-results/{handleId}` or `.../operation-results/{handleId}/$value` for the ValueOnly serialization.
:::

Depending on the in & inoutput arguments, the payload should look like this.

```{code-block} json
:caption: Example payload for invoking operations synchronously
:lineno-start: 1
{
	"inputArguments": [ {
		"value": {
			"modelType": "Property",
			"value": "4",
			"valueType": "xs:int",
			"idShort": "in"
		},
		// additional input arguments
	} ],
	"inoutputArguments": [ {
		"value": {
			"modelType": "Property",
			"value": "original value",
			"valueType": "xs:string",
			"idShort": "note"
		},
		// additional inoutput arguments
	} ],
	"clientTimeoutDuration": "PT10S"   // ISO8601 duration, here: 10 seconds
}
```

An easier, or at least less verbose way, of invoking operations is by using the ValueOnly serialization.
For this, add `/$value` to the end of the URL, i.e. resulting in either `.../invoke/$value` or `.../invoke-async/$value`.
The payload will be simplified and look similar to this

```{code-block} json
:caption: Example payload for invoking operations with ValueOnly
:lineno-start: 1
{
	"inputArguments": {
		"in": 4
	},
	"inoutputArguments": {
		"note": "original value"
	},
	"clientTimeoutDuration": "PT10S"
}
```

## OPC UA

The OPC UA Endpoint allows accessing data and execute operations within the FA³ST Service via [OPC UA](https://opcfoundation.org/about/opc-technologies/opc-ua/).

Unfortunately, there is currently no official mapping of the AAS API to OPC UA for AAS v3.0.
Nevertheless, FA³ST Service decided to still provide an OPC UA endpoint even though it is not (yet) standard-compliant.
This implementation is based on the [OPC UA Companion Specification OPC UA for Asset Administration Shell (AAS)](https://opcfoundation.org/developer-tools/specifications-opc-ua-information-models/opc-ua-for-i4-asset-administration-shell/) which defines a mapping between AAS and OPC UA for AAS v2.0 enriched with some custom adjustments and extensions to be used with AAS v3.0.

The OPC UA Endpoint is built with the [Prosys OPC UA SDK for Java](https://www.prosysopc.com/products/opc-ua-java-sdk/) which means in case you want to compile the OPC UA Endpoint yourself, you need a valid license for the SDK (which you can buy [here](https://www.prosysopc.com/products/opc-ua-java-sdk/purchase/).
For evaluation purposes, you also have the possibility to request an [evaluation license](https://www.prosysopc.com/products/opc-ua-java-sdk/evaluate).
However, this is not necessary for using the OPC UA Endpoint we already provide a pre-compiled version that is used by default when building FA³ST Service from code.
The developers of the Prosys OPC UA SDK have been so kind to allow us to publish that pre-compiled version as part of this open-source project under the condition that all classes related to their SDK are obfuscated.


### Configuration Parameters

OPC UA Endpoint configuration supports the following configuration parameters

| Name                                      | Allowed Value                                                                                        | Description                                                                                                                        | Default Value                                                              |
| ----------------------------------------- | ---------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------- |
| discoveryServerUrl<br>*(optional)*        | String                                                                                               | URL of the discovery server.<br>If empty, discovery server registration is disabled.                                               |                                                                            |
| secondsTillShutdown<br>*(optional)*       | Integer                                                                                              | The number of seconds the server waits for clients to disconnect                                                                   | 2                                                                          |
| serverCertificateBasePath<br>*(optional)* | String                                                                                               | Path where the server application certificates are stored                                                                          | PKI/CA                                                                     |
| supportedAuthentications<br>*(optional)*  | Anonymous<br>UserName<br>Certificate                                                                 | List of supported authentication types                                                                                             | Anonymous                                                                  |
| supportedSecurityPolicies<br>*(optional)* | NONE<br>BASIC128RSA15<br>BASIC256<br>BASIC256SHA256<br>AES128_SHA256_RSAOAEP<br>AES256_SHA256_RSAPSS | List of supported security policies                                                                                                | NONE,<br>BASIC256SHA256,<br>AES128_SHA256_RSAOAEP,<br>AES256_SHA256_RSAPSS |
| tcpPort<br>*(optional)*                   | Integer                                                                                              | The port to use for TCP                                                                                                            | 4840                                                                       |
| userMap<br>*(optional)*                   | Map<String, String>                                                                                  | A map containing usernames and password.<br>If *UserName* is not included in `supportedAuthentications`, this property is ignored. | *empty*                                                                    |
| userCertificateBasePath<br>*(optional)*   | String                                                                                               | Path where the certificates for user authentication are saved                                                                      | USERS_PKI/CA                                                               |

### Certificate Management

The path provided with the `serverCertificateBasePath` configuration property stores the server and client application certificates and contains the following subdirectories

- `/certs`: trusted client certificates
- `/crl`: certificate revocation list for client certificates
- `/issuers/certs`: certificates of trusted CAs
- `/issuers/crl`: certificate revocation list for CA certificates
- `/issuers/rejected`:	rejected CA certificates
- `/private`: certificates for the OPC UA server
- `/rejected`: unkown/rejected client certificates

To provision the OPC UA Endpoint to use an existing certificate for the server, save the certificate file as `{serverCertificateBasePath}/private/Fraunhofer IOSB AAS OPC UA Server@{hostname}_2048.der` and the private key as `{serverCertificateBasePath}/private/Fraunhofer IOSB AAS OPC UA Server@{hostname}_2048.pem` where `{hostname}` is the host name of your machine.

When an unkown client connects to the OPC UA Endpoint, the connection will be rejected and its client certificate will be stored in `/rejected`.
To trust the certificate of a client and allow the connection, move the file to `/certs`.

The path provided with the `userCertificateBasePath` configuration property stores the user certificates and contains the following subdirectories

- `/certs`: trusted user certificates
- `/crl`: certificate revocation list for user certificates
- `/issuers/certs`: certificates of trusted CAs
- `/issuers/crl`: certificate revocation list for CA certificates
- `/issuers/rejected`:	rejected CA certificates
- `/rejected`: unkown/rejected client certificates

Similar to the client certificates, unknown user certificates are stored in `/rejected` the first time a new certificate is encountered.
To trust this certificate, simply move it to `/certs`.

and `userCertificateBasePath` point to directories where the corresponding certificates are stored.
These directories contain the following subdirectories:


```{code-block} json
:caption: Example configuration for OPC UA Endpoint.
:lineno-start: 1

{
	"endpoints": [ {
			"@class": "de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.OpcUaEndpoint",
			"tcpPort" : 18123,
			"secondsTillShutdown" : 5,
			"discoveryServerUrl" : "opc.tcp://localhost:4840",
			"userMap" : {
			  "user1" : "secret"
			},
			"serverCertificateBasePath" : "PKI/CA",
			"userCertificateBasePath" : "USERS_PKI/CA",
			"supportedSecurityPolicies" : [ "NONE", "BASIC256SHA256", "AES128_SHA256_RSAOAEP" ],
			"supportedAuthentications" : [ "Anonymous", "UserName" ]
	} ],
	//...
}
```
### OPC UA Client Libraries

To connect to the OPC UA Endpoint, you need an OPC UA Client. Here are some example libraries and tools you can use:

- [Eclipse Milo](https://github.com/eclipse/milo): Open Source SDK for Java.
- [Unified Automation UaExpert](https://www.unified-automation.com/downloads/opc-ua-clients.html): Free OPC UA test client (registration on website required for download).
- [Prosys OPC UA Browser](https://www.prosysopc.com/products/opc-ua-browser/): Free OPC UA test client (registration on website required for download).
- [Official Samples from the OPC Foundation](https://github.com/OPCFoundation/UA-.NETStandard-Samples): C#-based sample code from the OPC Foundation.



```{figure} ../images/opc-ua-endpoint.png
:width: 800px
:align: center
Screenshot showing UaExpert connected to a FA³ST Service via OPC UA Endpoint.
```

### API

As stated, there is currently no official mapping of the AAS API to OPC UA for AAS v3.0 but FA³ST Service implements its proprietary adaption of the mapping for AAS v2.0.


#### Supported Functionality

- Writing values for the following types
	- Property
	- Range
	- Blob
	- MultiLanguageProperty
	- ReferenceElement
	- RelationshipElement
	- Entity
- Operations (OPC UA method calls). Exception: Inoutput-Variables are not supported in OPC UA.


#### Not (yet) Supported Functionality

- Updating the model, i.e., adding new elements at runtime is not possible
- Writing values for the following types
	- DataSpecifications
	- Qualifier
	- Category
	- ModelingKind
- AASDataTypeDefXsd
	- Base64Binary
	- UnsignedInt
	- UnsignedLong
	- UnsignedShort
	- UnsignedByte
