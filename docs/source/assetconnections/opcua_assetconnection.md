# OPC UA Asset Connection

## Supported Providers

-   ValueProvider
    -   read ✔️
	-   write ✔️
-   OperationProvider ✔️
-   SubscriptionProvider ✔️

## Configuration Parameters

### Asset Connection

| Name | Allowed Value | Description |
|:--| -- | -- |
| host | String | URL of the OPC UA server, e.g. _opc.tcp://localhost:4840_ |
| userTokenType | Enum | _optional_ User Token Type for connecting to the OPC UA server. Possible values are: Anonymous, UserName, Certificate. Default value is Anonymous |
| username | String | _optional_ Username for connecting to the OPC UA server. This value is required if userTokenType UserName is selected. |
| password | String | _optional_ Password for connecting to the OPC UA server. This value is required if userTokenType UserName is selected. |
| requestTimeout | int | _optional_ Timeout for requests (in ms), default: 3000 |
| acknowledgeTimeout | int | _optional_ Timeout for acknowledgement (in ms), default: 10000 |
| retries | int | _optional_ Number of times a request/connection should be retried after failing, default: 1 |
| securityPolicy | Enum | _optional_ Desired Security Policy for the connection to the OPC UA server. Possible values are: None, Basic256Sha256, Aes128_Sha256_RsaOaep and Aes256_Sha256_RsaPss. Default value is None. |
| securityMode | Enum | _optional_ Security Mode for the connection to the OPC UA server. Possible values are: None, Sign and SignAndEncrypt. Default value is None. |
| transportProfile | Enum | _optional_ Transport Profile for the connection to the OPC UA server. Possible values are: TCP_UASC_UABINARY, HTTPS_UABINARY, HTTPS_UAXML, HTTPS_UAJSON, WSS_UASC_UABINARY, WSS_UAJSON. Default value is TCP_UASC_UABINARY |
| securityBaseDir | String | _optional_ Base directory for the certificate handling. Default value is the current directory ("."). |
| applicationCertificateFile | File | _optional_ File name for the application certificate file. The format must be PKCS12. The file must contain exaxctly one alias. Default value is "application.p12". |
| applicationCertificatePassword | String | _optional_ Password for the application certificate file. Default value is an empty string ("") |
| authenticationCertificateFile | File | _optional_ File name for the authentication certificate file. The format must be PKCS12. This value is required if userTokenType Certificate is selected |
| authenticationCertificatePassword | String | _optional_ Password for the authentication certificate file. Default value is an empty string (""). This value is required if userTokenType Certificate is selected |

Please be aware, that when the Security Mode is set to a value other than "None", you must take care of the certificate management.
On the one hand, you must trust the certificate of the Asset Connection Client in the OPC UA server.
On the other hand, you must trust the certificate of the OPC UA server in the Asset Connection.
The certificates of the Asset Connection are stored in the securityBaseDir.
The certificate of the Asset Connection is specified in applicationCertificateFile. If no certificate is provided, a self-signed certificate is created on startup.
In the subdirectory "pki\rejected" of the securityBaseDir the certificates of unknown or rejected servers are saved, the certificates of trusted servers are saved in the subdirectory "pki\trusted\certs". You can also use this directory to trust certificates of a certificate authority, for the corresponding certificate revocation list (CRL) use the directory "pki\trusted\crl".
Alternatively you can also trust certificates of a certificate authority using the directory "pki\issuers\certs" for the certificate and "pki\issuers\crl" for the CRL.

As already said, when you first connect to an OPC UA Server using a Security Policy like Basic256Sha256, you have to make sure, that the OPC UA Server trusts the certificate of the Asset Connection Client. Afterwards, when you try to connect to the server, the certificate of the server will be rejected initially and saved in the directory for the rejected certificates ("pki\rejected" in the securityBaseDir).
To trust the server, move the corresponding certificate file from the rejected directory to the trusted directory ("pki\trusted\certs" in the securityBaseDir).

### Value Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| nodeId | String | NodeId of the the OPC UA node to read/write in ExpandedNodeId format |
| arrayIndex | String | _optional_ Index of the desired array element if the value is an array |

All NodeIds (also below) are specified in the ExpandedNodeId format (see [OPC UA Reference, Part 6](https://reference.opcfoundation.org/v104/Core/docs/Part6/5.3.1/), Section ExpandedNodeId). In the following you can see two examples.

If the value is an array, it's possible to reference a specific element of the array. The index of the desired element is specified with square brackets, e.g. "[2]".  If the value is multi-dimensional array, multiple indizes can be specified, e.g. "&#091;1&#093;&#091;3&#093;".

#### Example

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

### Operation Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| nodeId | String | nodeId of the OPC UA method to call in ExpandedNodeId format |
| parentNodeId | String | _optional_ nodeId of the OPC UA object in ExpandedNodeId format, in which the method is contained. When no parentNodeId is given here, the parent object of the method is used |
| inputArgumentMapping | List&lt;ArgumentMapping&gt; | _optional_ list of mappings for input arguments between the idShort of a SubmodelElement and an argument name
| outputArgumentMapping | List&lt;ArgumentMapping&gt; | _optional_ list of mappings for output arguments between the idShort of a SubmodelElement and an argument name

#### Example

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

### Subscription Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| nodeId | String | NodeId of the the OPC UA node to read/write in ExpandedNodeId format |
| interval | long | Interval to poll the server for changes (in ms) _currently not used_
| arrayIndex | String | _optional_ Index of the desired array element if the value is an array |

If the value is an array, it's possible to reference a specific element of the array. The index of the desired element is specified with square brackets, e.g. "[2]".  If the value is multi-dimensional array, multiple indizes can be specified, e.g. "&#091;1&#093;&#091;3&#093;".

#### Example

```json
{
	"nodeId": "nsu=com:example;s=foo",
	"interval": 1000,
	"arrayIndex" : "[2]"
}
```

## Complete Example

A complete example for OPC UA asset connection could look like this

```json

{
	"@class": "de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.OpcUaAssetConnection",
	"host": "opc.tcp://localhost:4840",
	"securityPolicy": "None",
	"securityMode" : "None",
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
