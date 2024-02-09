# MQTT Asset Connection

## Supported Providers

-   ValueProvider
    -   read ❌
	-   write ✔️
-   OperationProvider ❌
-   SubscriptionProvider ✔️

## Configuration Parameters

### Asset Connection

| Name | Allowed Value | Description |
|:--| -- | -- |
| serverUri | String | URL of the MQTT server, e.g. _tcp://localhost:1883_ |
| clientId | String | _optional_ Id of the MQTT client used to connect to the server, default: random value |
| username | String | _optional_ Username for connecting to the MQTT server |
| password | String | _optional_ Password for connecting to the MQTT server |

### Value Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| format | JSON\|XML | Content format of payload |
| topic | String | MQTT topic to use |
| template | String | _optional_ Template used to format payload |

#### Example

```json
{
	"format": "JSON",
	"topic": "example/myTopic",
	"template": "{\"foo\" : \"${value}\"}"
}
```

### Subscription Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| format | JSON\|XML | Content format of payload |
| topic | String | MQTT topic to use |
| query | String | _optional_ Additional information how to extract actual value from received messages, depends on `format`, e.g. for JSON this is a JSON Path expression. |

#### Example

```json
{
	"format": "JSON",
	"topic": "example/myTopic",
	"query": "$.foo"
}
```
