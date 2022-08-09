# OPC UA AssetConnection
The OPC UA asset connection supports the following functionality:

-   `ValueProvider`: fully supported (read/write)
-   `OperationProvider`: invoke operations synchroniously, async invocation not supported
-   `SubscriptionProvider`: fully supported

**Configuration Parameters**
-   on connection level
-   `host`: URL of the OPC UA server. Please be sure that the URL starts with `opc.tcp://`.
-   on ValueProdiver level
-   `nodeId`: nodeId of the the OPC UA node to read/write
-   on OperationProdiver level
-   `nodeId`: nodeId of the the OPC UA node representing the OPC UA method to invoke
-   on SubscriptionProdiver level
-   `nodeId`: nodeId of the the OPC UA node to subscribe to
-   `interval`: subscription interval in ms

Example configuration for a subscription provider:

```json
{
	"nodeId": "some.node.id.property",
	"interval": 1000
}
```

A concrete example for OPC UA asset connection could look like this
```json

{
	"@class": "de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.OpcUaAssetConnection",
	"host": "opc.tcp://localhost:4840",
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