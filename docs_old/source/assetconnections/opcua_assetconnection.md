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
| securityPolicy | Enum | _optional_ Desired Security Policy for the connection to the OPC UA server. Possible values are: None, Basic256Sha256, Aes128_Sha256_RsaOaep and Aes256_Sha256_RsaPss. Default value is None. |
| securityMode | Enum | _optional_ Security Mode for the connection to the OPC UA server. Possible values are: None, Sign and SignAndEncrypt. Default value is None. |
| transportProfile | Enum | _optional_ Transport Profile for the connection to the OPC UA server. Possible values are: TCP_UASC_UABINARY, HTTPS_UABINARY, HTTPS_UAXML, HTTPS_UAJSON, WSS_UASC_UABINARY, WSS_UAJSON. Default value is TCP_UASC_UABINARY |
| securityBaseDir | String | _optional_ Base directory for the certificate handling. Default value is the current directory ("."). |
| applicationCertificate | Object | _optional_  The application certificate [See details](../../gettingstarted/configuration#providing-certificates-in-configuration) |
| authenticationCertificate | Object | _optional_  The authentication certificate [See details](../../gettingstarted/configuration#providing-certificates-in-configuration) |

#### Remarks on certificate management
In OPC UA , certificates can be used for two purposes:
- encryption & signing of messages, and
- authentication of a client.

We call the certificate used of encryption _application certificate_ and the one used for authenticating a client _authentication certificate_.
You can choose to use only one of these options or both.
If using both, you can use different or the same certificates.

#### Application Certificate
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

#### Authentication Certificate
Which authentication certificate is used is determined by a similar logic as for the application certificate besides that this certificate is not auto-generated if not present:
- `authenticationCertificate.keyStorePath` if it is an absolute file path and the file exists (default: application.p12)
- `{securityBaseDir}/{authenticationCertificate.keyStorePath}` if the file exists (default: `./{authenticationCertificate.keyStorePath}`)


### Value Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| nodeId | String | NodeId of the the OPC UA node to read/write in ExpandedNodeId format |
| arrayIndex | String | _optional_ Index of the desired array element if the value is an array |

All NodeIds (also below) are specified in the ExpandedNodeId format (see [OPC UA Reference, Part 6](https://reference.opcfoundation.org/v104/Core/docs/Part6/5.3.1/), Section ExpandedNodeId). In the following you can see two examples.

If the value is an array, it’s possible to reference a specific element of the array. The index of the desired element is specified with square brackets, e.g. “[2]”. If the value is a multidimensional array, multiple indices can be specified, e.g. "&#091;1&#093;&#091;3&#093;".

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
| inputArgumentMapping | List&lt;ArgumentMapping&gt; | _optional_ list of mappings for input arguments between the idShort of a SubmodelElement and an argument name |
| outputArgumentMapping | List&lt;ArgumentMapping&gt; | _optional_ list of mappings for output arguments between the idShort of a SubmodelElement and an argument name |

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
| interval | long | Interval to poll the server for changes (in ms), default: 1000, _currently not used_ |
| arrayIndex | String | _optional_ Index of the desired array element if the value is an array |

If the value is an array, it's possible to reference a specific element of the array. The index of the desired element is specified with square brackets, e.g. "[2]".  If the value is multidimensional array, multiple indices can be specified, e.g. "&#091;1&#093;&#091;3&#093;".

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