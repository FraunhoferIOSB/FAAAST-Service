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
| username | String | _optional_ Username for connecting to the OPC UA server |
| password | String | _optional_ Password for connecting to the OPC UA server |
| securityPolicy | Enum | _optional_ Desired Security Policy for the connection to the OPC UA server. Possible values are: None, Basic256Sha256, Aes128_Sha256_RsaOaep and Aes256_Sha256_RsaPss. Default value is None. |

Please be aware, that when the Security Policy is set to a value other than "None", you must take care of the certificate management.
On the one hand, you must trust the certificate of the Asset Connection Client in the OPC UA server.
On the other hand, you must trust the certificate of the OPC UA server in the Asset Connection.
By default the certificates of the Asset Connection are stored in a subdirectory of the current directory.
You can change this directory by specifying a different directory in the environment variable FA3ST_ASSET_CONN_PKI.
Below this directory you can find the certificate of the Asset Connection in the file "client\security\fa3st-client.pfx".
In the directory "client\security\pki\rejected" the certificates of unknown or rejected servers are saved, the certificates of trusted servers are saved in the directory "client\security\pki\trusted\certs". You can also use this directory to trust certificates of a certificate authority, for the corresponding certificate revocation list (CRL) use the directory "client\security\pki\trusted\crl".
Alternatively you can also trust certificates of a certificate authority using the directory "client\security\pki\issuers\certs" for the certificate and "client\security\pki\issuers\crl" for the CRL.

As already said, when you first connect to an OPC UA Server using a Security Policy like Basic256Sha256, you have to make sure, that the OPC UA Server trusts the certificate of the Asset Connection Client. Afterwards, when you try to connect to the server, the certificate of the server will be rejected initially and saved in the directory for the rejected certificates ("client\security\pki\rejected").
To trust the server, move the corresponding certificate file from the rejected directory to the trusted directory (client\security\pki\trusted\certs").

### Value Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| nodeId | String | nodeId of the the OPC UA node to read/write in ExpandedNodeId format |

All NodeIds (also below) are specified in the ExpandedNodeId format (see [OPC UA Reference, Part 6](https://reference.opcfoundation.org/v104/Core/docs/Part6/5.3.1/), Section ExpandedNodeId). In the following you can see two examples.

#### Example

```json
{
	"nodeId": "nsu=com:example;s=foo"
}
```

or

```json
{
	"nodeId": "ns=2;s=foo"
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
| nodeId | String | nodeId of the the OPC UA node to read/write in ExpandedNodeId format |
| interval | long | Interval to poll the server for changes (in ms) _currently not used_

#### Example

```json
{
	"nodeId": "nsu=com:example;s=foo",
	"interval": 1000
}
```

## Complete Example

A complete example for OPC UA asset connection could look like this

```json

{
	"@class": "de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.OpcUaAssetConnection",
	"host": "opc.tcp://localhost:4840",
	"securityPolicy": "None",
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
