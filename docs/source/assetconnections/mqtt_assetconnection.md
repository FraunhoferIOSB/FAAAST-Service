# MQTT

## Supported Providers

-   `ValueProvider`
    -   read ❌
	-   write ✔️
-   `OperationProvider` ❌
-   `SubscriptionProvider` ✔️

## Configuration Parameters

### Asset Connection

| Name | Allowed Value | Description |
|:--| -- | -- |
| `serverUri` | String | URL of the MQTT server, e.g. _tcp://localhost:1883_ |
| `clientId` | String | [optional] Id of the MQTT client used to connect to the server, default: random value |
| `username` | String | [optional] Username for connecting to the MQTT server |
| `password` | String | [optional] Password for connecting to the MQTT server |

### Value Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| `topic` | String | MQTT topic to use |
| `format` | JSON|XML | Content format of payload, default: JSON |
| `template` | String | Template used to format payload

#### Example

```json
{
	"topic": "example/myTopic",
	"format": "JSON",
	"template": "{\"foo\" : \"${value}\"}"
}
```

### Subscription Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| `topic` | String | MQTT topic to use |
| `format` | JSON|XML | Content format of payload, default: JSON |
| `query` | String | Additional information how to extract actual value from received messages, depends on `format`, e.g. for JSON this is a JSON Path expression.

#### Example

```json
{
	"topic": "example/myTopic",
	"format": "JSON",
	"query": "$.foo"
}
```
