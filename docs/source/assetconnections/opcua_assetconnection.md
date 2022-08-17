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

### Value Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| nodeId | String | nodeId of the the OPC UA node to read/write  |

#### Example

```json
{
	"nodeId": "ns=com:example;s=foo"
}
```

### Operation Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| nodeId | String | nodeId of the the OPC UA node to read/write  |

#### Example

```json
{
	"nodeId": "ns=com:example;s=foo"
}
```

### Subscription Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| nodeId | String | nodeId of the the OPC UA node to read/write  |
| interval | long | Interval to poll the server for changes (in ms) _currently not used_

#### Example

```json
{
	"nodeId": "ns=com:example;s=foo",
	"interval": 1000
}
```

## Complete Example

A complete example for OPC UA asset connection could look like this
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