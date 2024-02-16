# Endpoint

`Endpoint` implementations allow to communicate with the AAS from the outside, e.g. users or external applications. 
An instance of FA³ST Service can serve multiple endpoints at the same time.
Endpoints will be synchronized, meaning if a FA³ST Service offers multiple endpoint such as HTTP(S) and OPC UA at the same time, changes done via one of the endpoints like updating a value is reflected in the other.

The following is an example of the relevant part of the configuration part comprising both an HTTP(S) and OPC UA endpoint

```json
"endpoints": [
	{
		"@class": "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint",
		"port": 8080,
		"corsEnabled": true
	},
	{
		"@class": "de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.OpcUaEndpoint",
		"tcpPort": 8081
	}
]
```

## HTTP


The HTTP Endpoint allows accessing data and execute operations within the FA³ST Service via REST-API.
It will always use HTTPS. The HTTP Endpoint is based on the document [Details of the Asset Administration Shell - Part 2](https://www.plattform-i40.de/IP/Redaktion/EN/Downloads/Publikation/Details_of_the_Asset_Administration_Shell_Part2_V1.html), _Interoperability at Runtime –
Exchanging Information via Application
Programming Interfaces (Version 1.0RC02)_' , November 2021 and the OpenAPI documentation [DotAAS Part 2 | HTTP/REST | Entire Interface Collection](https://app.swaggerhub.com/apis/Plattform_i40/Entire-API-Collection/V1.0RC02), Apr, 26th 2022

### Configuration Parameters

| Name | Allowed Value | Description                                                                                                                                                                                                                     |
|:--| -- |---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| port | Integer | _optional_ The port to use, default: 8080                                                                                                                                                                                       |
| corsEnabled | Boolean | _optional_ If Cross-Origin Resource Sharing (CORS) should be enabled, typically required if you want to access the REST interface from any machine other than the one running FA³ST Service, default: false                     |
| sniEnabled | Boolean | _optional_ If Server Name Identification (SNI) should be enabled. THis should only be disabled for testing purposes as it may present a security risk., default: true                     |
| certificate; | Object | _optional_  The HTTPS certificate to use, if none is provided a self-signed certificate will be generated [See details](../../gettingstarted/configuration#providing-certificates-in-configuration) |

#### Example

In order to use the HTTP Endpoint the configuration settings require to include an HTTP Endpoint configuration, like the one below:
```json
{
	"endpoints": [
		{
			"@class": "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint",
			"port": 8080,
			"corsEnabled": true,
			"sniEnabled": true,
			"certificate": {
				"keyStoreType": "PKCS12",
				"keyStorePath": "C:\faaast\MyKeyStore.p12",
				"keyStorePassword": "changeit",
				"keyAlias": "server-key",
				"keyPassword": "changeit"
			}
		}
	]
}
```

### API

#### Supported API calls
-   Current API prefix
    -   /api/v3.0
-   Asset Administration Shell Repository Interface
    -   /shells ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen)
    -   /shells/{aasIdentifier} ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)

-   Asset Administration Shell Interface
    -   /shells/{aasIdentifier} ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange)
    -   /shells/{aasIdentifier}/asset-information ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange)
    -   /shells/{aasIdentifier}/asset-information/thumbnail ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)
    -   /shells/{aasIdentifier}/submodel-refs ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen)
    -   /shells/{aasIdentifier}/submodel-refs/{submodelIdentifier} ![DELETE](https://img.shields.io/badge/DELETE-red)

-   Submodel Repository Interface
    -   /submodels ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen)
    -   /submodels/{submodelIdentifier} ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)

-   Submodel Interface
    -   /submodels/{submodelIdentifier} ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange)
    -   /submodels/{submodelIdentifier}/submodel-elements ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen)
    -   /submodels/{submodelIdentifier}/submodel-elements/{idShortPath} ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)
    -   /submodels/{submodelIdentifier}/submodel-elements/{idShortPath} ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)
    -   /submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/invoke ![POST](https://img.shields.io/badge/POST-brightgreen)
    -   /submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/operation-results/{handle-Id} ![GET](https://img.shields.io/badge/GET-blue)

-   Submodel Interface (combined with Asset Administration Shell Interface)
    -   /shells/{aasIdentifier}/submodels/{submodelIdentifier} ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange)
    -   /shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel-elements ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen)
    -   /shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel-elements/{idShortPath} ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen), ![PUT](https://img.shields.io/badge/PUT-orange), ![DELETE](https://img.shields.io/badge/DELETE-red)
    -   /shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/attachment ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange), ![DELETE](https://img.shields.io/badge/DELETE-red)
    -   /shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/invoke ![POST](https://img.shields.io/badge/POST-brightgreen)
    -   /shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/operation-results/{handle-Id} ![GET](https://img.shields.io/badge/GET-blue)

-   Concept Description Repository Interface
    -   /concept-descriptions ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen)
    -   /concept-descriptions/{cdIdentifier} ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)

-   Asset Administration Shell Basic Discovery
    -   /lookup/shells ![GET](https://img.shields.io/badge/GET-blue)
    -   /lookup/shells/{aasIdentifier} ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen) ![DELETE](https://img.shields.io/badge/DELETE-red)

-   Asset Administration Shell Serialization Interface
    -   /serialization ![GET](https://img.shields.io/badge/GET-blue)

#### Optional query parameters

-   level=deep|core
-   content=normal|value|path
-   extent=WithoutBLOBValue/WithBLOBValue
-   InvokeOperation supports async=true/false

They are added to the URL as regular query params

```sh
https://url:port?level=deep&content=value
```

FA³ST Service currently supports only content=value and content=normal


#### Unsupported API calls

-   Asset Administration Shell Registry Interface (out-of-scope)
-   Submodel Registry Interface (out-of-scope)
-   AASX File Server Interface (probably supported in future)
    -   /packages
    -   /packages/{packageId}

### Example

Sample HTTP Call for Operation _GetSubmodelElementByPath_
using the parameters
-   _submodelIdentifier_: https://acplt.org/Test_Submodel (must be base64URL-encoded)
-   _idShortPath_: ExampleRelationshipElement (must be URL-encoded)

using the query-parameters _level=deep_ and _content=normal_.

> To avoid problems with IRIs in URLs the identifiers shall be BASE64-URL-encoded before using them as parameters in the HTTP APIs. IdshortPaths are URL-encoded to handle including square brackets.

```sh
https://localhost:8080/submodels/aHR0cHM6Ly9hY3BsdC5vcmcvVGVzdF9TdWJtb2RlbA==/submodel/submodel-elements/ExampleRelationshipElement?level=deep&content=normal
```


## OPC UA

The OPC UA Endpoint allows accessing data and execute operations within the FA³ST Service via OPC UA.
For detailed information on OPC UA see
[About OPC UA](https://opcfoundation.org/about/opc-technologies/opc-ua/)

The OPC UA Endpoint is based on the [OPC UA Companion Specification OPC UA for Asset Administration Shell (AAS)](https://opcfoundation.org/developer-tools/specifications-opc-ua-information-models/opc-ua-for-i4-asset-administration-shell/).
The release version of this Companion Specification is based on the document [Details of the Asset Administration Shell - Part 1 Version 2](https://www.plattform-i40.de/IP/Redaktion/EN/Downloads/Publikation/Details_of_the_Asset_Administration_Shell_Part1_V2.html).

This implementation is based on [Details of the Asset Administration Shell - Part 1 Version 3](https://www.plattform-i40.de/IP/Redaktion/EN/Downloads/Publikation/Details_of_the_Asset_Administration_Shell_Part1_V3.html).
Therefore, the current implementation is actually not compatible with the Companion Specification.

The OPC UA Endpoint is built with the [Prosys OPC UA SDK for Java](https://www.prosysopc.com/products/opc-ua-java-sdk/).
If you want to build the OPC UA Endpoint, you need a valid license for the SDK.

You can purchase a [Prosys OPC UA License](https://www.prosysopc.com/products/opc-ua-java-sdk/purchase/). As the OPC UA Endpoint is a server, you need a "Client & Server" license.

For evaluation purposes, you also have the possibility to request an [evaluation license](https://www.prosysopc.com/products/opc-ua-java-sdk/evaluate).

### Configuration Parameters

OPC UA Endpoint configuration supports the following configuration parameters
| Name | Allowed Value | Description                                                                                                                                                                                                                     |
|:--| -- |---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| tcpPort | Integer | _optional_ The port to use, default: 4840 |
| secondsTillShutdown | Integer | _optional_ The number of seconds the server waits for clients to disconnect, default: 2 |
| discoveryServerUrl | String | _optional_ URL of the discovery server, default: empty String |
| serverCertificateBasePath | String | _optional_ Path where the server application certificates are stored, default: "PKI/CA" |
| supportedSecurityPolicies | List | _optional_ List of supported security Policies, default: "NONE", "BASIC256SHA256", "AES128_SHA256_RSAOAEP" and "AES256_SHA256_RSAPSS" |
| supportedAuthentications | List | _optional_ List of supported authentication types, default: "Anonymous" |
| userMap | Map | _optional_ Map with user authentication credentials, default: empty |
| userCertificateBasePath | String | _optional_ Path where the certificates for user authentication are saved, default: "USERS_PKI/CA" |

-   `tcpPort` is the desired Port for the OPC UA TCP Protocol (opc.tcp). Default is 4840.
-   `secondsTillShutdown` is the number of seconds the server waits for clients to disconnect when stopping the Endpoint. When the Endpoint is stopped, the server sends a predefined event to all connected clients, that the OPC UA Server is about to shut down. Now, the OPC UA Server waits the given number of seconds before he stops, to give the clients the possibility to disconnect from the Server. When `secondsTillShutdown` is 0, the Endpoint stops immediately. Default is 2.
-   `discoveryServerUrl` is the URL which is used for registration with a discovery server. An empty String disables discovery server registration. Default is an empty String.
-   `serverCertificateBasePath` is the path where the server application certificates are stored. Default is "PKI/CA". Below this path, further subdirectories are created. In "private" the certificates and private keys of the OPC UA Endpoint are saved. The filename of the base server application certificate is "Fraunhofer IOSB AAS OPC UA Server@ILT808_2048.der", the filename of the corresponding private key is "Fraunhofer IOSB AAS OPC UA Server@ILT808_2048.pem". If this application certificate is missing, a self-signed certificate is automatically created on start. In "rejected" unknown (rejected) certificates from connecting clients are saved. In "certs" trusted certificates for clients are saved. To trust the certificate of a client, move it from "rejected" to "certs". In "crl" the certificate revocation list for a CA certificate saved in "certs" is saved. In "issuers/certs" the certificates of trusted CAs are saved. In "issuers/crl" the certificate revocation lists of the corresponding trusted CA certificates are saved.
-   `supportedSecurityPolicies` is the list of supported security Policies. The security Policies included in the list will be available in the OPC UA Endpoint. Possible values are: NONE, BASIC128RSA15, BASIC256, BASIC256SHA256, AES128_SHA256_RSAOAEP, AES256_SHA256_RSAPSS. If the list is empty, the default value is "NONE", "BASIC256SHA256", "AES128_SHA256_RSAOAEP" and "AES256_SHA256_RSAPSS". Please note, that BASIC128RSA15 and BASIC256 are deprecated, as they are considered unsafe.
-   `supportedAuthentications` is the list of supported authentication types. The authentication types included in the list will be available in the OPC UA Endpoint. Possible values are: Anonymous, UserName, Certificate. If the list is empty, the default value is "Anonymous".
-   `userMap` is a map with user authentication credentials for the OPC UA Endpoint. The key of the map is the username and the value is the password. If the map is empty, authentication with username and password is not possible. This value is only relevant, when "UserName" is included in `supportedAuthentications`.
-   `userCertificateBasePath` is the path where the certificates for user authentication are saved. Default is "USERS_PKI/CA". Below this path, further subdirectories are created. In "rejected", certificates from unknown (rejected) users are saved. In "certs" certificates for trusted users are saved. To trust the certificate of a user, move it from "rejected" to "certs". In "crl" the certificate revocation list for a corresponding CA certificate saved in "certs" is saved. In "issuers/certs" the certificates of trusted CAs are saved. In "issuers/crl" the certificate revocation lists of the corresponding trusted CA certificates are saved. This value is only relevant, when "Certificate" is included in `supportedAuthentications`.

#### Example

In order to use the OPC UA Endpoint, the configuration settings require to include an OPC UA Endpoint configuration, like the one below:
```json
{
	"endpoints": [
		{
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
		}
	]
}
```

To connect to the OPC UA Endpoint, you need an OPC UA Client.
Here are some examples of OPC UA Clients:
-   [Unified Automation UaExpert](https://www.unified-automation.com/downloads/opc-ua-clients.html)
UaExpert is a free test client for OPC UA. A registration for the website is required.

-   [Prosys OPC UA Browser](https://www.prosysopc.com/products/opc-ua-browser/)
Free Java-based OPC UA Client. A registration for the website is required.

-   [Official Samples from the OPC Foundation](https://github.com/OPCFoundation/UA-.NETStandard-Samples)
C#-based sample code from the OPC Foundation.

-   [Eclipse Milo](https://github.com/eclipse/milo)
Java-based Open Source SDK for Java.

Here you can see a sample Screenshot with UaExpert.
![Screenshot with UaExpert](../images/OpcUaEndpoint.png "Screenshot with UaExpert")

### Supported Functions
-   Operations (OPC UA method calls). Exception: Inoutput-Variables are not supported in OPC UA.

-   Write Values
    -   Property
    -   Range
    -   Blob
    -   MultiLanguageProperty
    -   ReferenceElement
    -   RelationshipElement
    -   Entity

### Not (yet) Supported Functions
-   Events

-   Write Values
    -   DataSpecifications
    -   Qualifier
    -   Category
    -   ModelingKind

-   AASDataTypeDefXsd
    -   Base64Binary
    -   UnsignedInt
    -   UnsignedLong
    -   UnsignedShort
    -   UnsignedByte


