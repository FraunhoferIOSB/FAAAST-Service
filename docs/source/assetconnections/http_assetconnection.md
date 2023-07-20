# HTTP Asset Connection

## Supported Providers

-   ValueProvider
    -   read ✔️
	-   write ✔️
-   OperationProvider ✔️
-   SubscriptionProvider ✔️ (via polling)

## Configuration Parameters

### Asset Connection

| Name | Allowed Value | Description |
|:--| -- | -- |
| baseUrl | String | Base URL of the HTTP server, e.g. _http://example.com_ |
| username | String | _optional_ username for connecting to the HTTP server |
| password | String | _optional_ password for connecting to the HTTP server |
| headers | Map<String,String> | _optional_ headers to send with each request |
| trustedCertificates | Object | _optional_  information to load a key store containing certificates that should be trusted, i.e. when connecting to a server that is using self-signed certificates that by default would not be trusted. :ref:`See details<certificate-info>` |

### Value Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| format | JSON\|XML | content format of payload |
| path | String | Path for the HTTP request, relative to the `baseUrl` of the connection |
| headers | Map<String,String> | _optional_ headers to send with each request (overrides connection-level headers) |
| query | String | _optional_ additional information how to extract actual value from received messages, depends on `format`, e.g. for JSON this is a JSON Path expression |
| template | String | _optional_ template used to format payload when sending via HTTP |
| writeMethod | GET\|PUT\|POST | _optional_ HTTP method to use when writing a value to HTTP, default: PUT |

#### Example

```json
{
	"format": "JSON",
	"path": "/foo",
	"headers": {
		"foo": "bar"
	},
	"query": "$.foo",
	"template": "{\"foo\" : \"${value}\"}",
	"writeMethod": "POST"
}
```

### Operation Provider

| Name | Allowed Value | Description |
|:--| -- | -- |
| format | JSON\|XML | content format of payload |
| path | String | Path for the HTTP request, relative to the `baseUrl` of the connection |
| headers | Map<String,String> | _optional_ headers to send with each request (overrides connection-level headers) |
| method | PUT\|POST | _optional_ HTTP method to use, default: POST |
| template | String | _optional_ template used to format payload when sending via HTTP |
| queries | Map<String, String> | _optional_ Map of result variable idShorts and corresponding query expressions to fetch them from returned value, query expressions depend on `format`, e.g for JSON these are JSON Path expressions |


#### Example

Operation with input parameters `in1` and `in2` and output parameters `out1` and `out2`

```json
{
	"format": "JSON",
	"path": "/foo/execute",
	"headers": {
		"foo": "bar"
	},
	"method": "POST",	
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
| format | JSON\|XML | content format of payload |
| path | String | Path for the HTTP request, relative to the `baseUrl` of the connection |
| headers | Map<String,String> | _optional_ headers to send with each request (overrides connection-level headers) |
| interval | long | _optional_ Interval to poll the server for changes (in ms), default: 100 |
| method | GET\|PUT\|POST | _optional_ HTTP method to use, default: GET |
| payload | String | _optional_ Static content to send which each request |
| query | String | _optional_ additional information how to extract actual value from received messages, depends on `format`, e.g. for JSON this is a JSON Path expression |


#### Example

```json
{
	"path": "/foo",
	"headers": {
		"foo": "bar"
	},
	"interval": "500",
	"method": "GET",
	"template": "{\"foo\" : \"bar\"}"
}
```
