# Asset Connections
`AssetConnection` implementations allows connecting/synchronizing elements of your AAS to/with assets via different protocol. This functionality is further divided into  3 so-called provider, namely
-   [ValueProvider](https://github.com/FraunhoferIOSB/FAAAST-Service/blob/main/core/src/main/java/de/fraunhofer/iosb/ilt/faaast/service/assetconnection/AssetValueProvider.java), supporting reading and writing values from/to the asset, i.e. each time a value is read or written via an endpoint the request is forwarded to the asset
-   [OperationProvider](https://github.com/FraunhoferIOSB/FAAAST-Service/blob/main/core/src/main/java/de/fraunhofer/iosb/ilt/faaast/service/assetconnection/AssetOperationProvider.java), supporting the execution of operations, i.e. forwards operation invocation requests to the asset and returning the result value,
-   [SubscriptionProvider](https://github.com/FraunhoferIOSB/FAAAST-Service/blob/main/core/src/main/java/de/fraunhofer/iosb/ilt/faaast/service/assetconnection/AssetSubscriptionProvider.java), supporting synchronizing the AAS with pub/sub-based assets, i.e. subscribes to the assets and updates the AAS with new values over time.

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