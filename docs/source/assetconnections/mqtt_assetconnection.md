# MQTT Asset Connection
The MQTT asset connection supports the following functionality:

-   `ValueProvider`: write values only, read not supported
-   `OperationProvider`: not supported
-   `SubscriptionProvider`: subscribe to value changes

**Configuration Parameters**
-   on connection level
-   `serverUri`: URL of the MQTT server
-   `clientId`: id of the MQTT client (optional, default: random)
-   on ValueProdiver level
-   `topic`: MQTT topic to use
-   `contentFormat`: JSON|XML (default: JSON, currently, only JSON supported)
-   `query`: additional information how to format messages sent via MQTT - depends on `contentFormat`. For JSON this is a JSON Path expression.
-   on SubscriptionProdiver level
-   `topic`: MQTT topic to use
-   `contentFormat`: JSON|XML (default: JSON, currently, only JSON supported)
-   `query`: additional information how to extract actual value from received messages - depends on `contentFormat`. For JSON this is a JSON Path expression.

Example configuration for one of the providers:

```json
{
	"topic": "example/myTopic",
	"query": "$.property.value",
	"contentFormat": "JSON"
}
```