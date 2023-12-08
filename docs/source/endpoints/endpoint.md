# Endpoints
`Endpoint` implementations allow to communicate with the AAS from the outside, e.g. users or external applications. 
An instance of FA³ST Service can serve multiple endpoints at the same time.
Endpoints will be synchronized, meaning if a FA³ST Service offers multiple endpoint such as HTTP(S) and OPC UA at the same time, changes done via one of the endpoints like updating a value is reflected in the other.

The following is an example of the relevant part of the configuration part comprising both an HTTP(S) and OPC UA endpoint

```json
"endpoints": [
	{
		"@class": "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint",
		"port": 8080,
		"corsEnabled": true
	},
	{
		"@class": "de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.OpcUaEndpoint",
		"tcpPort": 8081
	}
]
```

