# HTTP

## Supported Providers

-   `ValueProvider`
    -   read ✔️
	-   write ✔️
-   `OperationProvider` ✔️
-   `SubscriptionProvider` ✔️ (via polling)

## Configuration Parameters

### Asset Connection

| Name | Allowed Value | Description |
|:--| -- | -- |
| `baseUrl` | String | Base URL of the HTTP server, e.g. _http://example.com_ |
| `username` | String | [optional] username for connecting to the HTTP server |
| `password` | String | [optional] password for connecting to the HTTP server |

### Value Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| `path` | String | Path for the HTTP request, relative to the `baseUrl` of the connection |
| `writeMethod` | GET|PUT|POST | HTTP method to use when writing a value to HTTP |
| `format` | JSON|XML | content format of payload, default: JSON |
| `query` | String | additional information how to extract actual value from received messages, depends on `format`, e.g. for JSON this is a JSON Path expression.
| `template` | String | template used to format payload when sending via HTTP

#### Example

```json
{
	"path": "/foo",
	"writeMethod": "POST",
	"format": "JSON",
	"query": "$.foo",
	"template": "{\"foo\" : \"${value}\"}"
}
```

### Operation Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| `path` | String | Path for the HTTP request, relative to the `baseUrl` of the connection |
| `method` | GET|PUT|POST | HTTP method to use |
| `format` | JSON|XML | content format of payload, default: JSON |
| `template` | String | template used to format payload when sending via HTTP
| `queries` | Map<String, String> | Map of result variable idShorts and corresponding query expressions to fetch them from returned value, query expressions depend on `format`, e.g for JSON these are JSON Path expressions.


#### Example

Operation with input parameters `in1` and `in2` and output parameters `out1` and `out2`

```json
{
	"path": "/foo/execute",
	"method": "POST",
	"format": "JSON",
	"template": "{\"input1\" : \"${in1}\", \"input2\" : \"${in2}\"}",
	"queries": {
		"out1": "$.output1",
		"out2": "$.output2"
	}
}
```

### Subscription Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| `path` | String | Path for the HTTP request, relative to the `baseUrl` of the connection |
| `method` | GET|PUT|POST | HTTP method to use |
| `payload` | String | Static content to send which each request |
| `interval` | long | Interval to poll the server for changes (in ms), default: 100

#### Example

```json
{
	"path": "/foo",
	"method": "GET",
	"template": "{\"foo\" : \"bar\"}",
	"interval": "500"
}
```
