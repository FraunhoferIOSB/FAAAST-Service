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
| requestTimeout | int | _optional_ Timeout for requests (in ms), default: 3000 |
| acknowledgeTimeout | int | _optional_ Timeout for acknowledgement (in ms), default: 10000 |

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
