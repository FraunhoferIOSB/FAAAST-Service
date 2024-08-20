## Common mistakes and frequently asked questions

### FA³ST Service does not load the AASX/JSON model

If you get a validation error like:
```
[ERROR] Model validation failed with the following error(s):
Found 2 violation(s):
Duplicate identifier 'https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/SpecificConditionsForUse' - identifiers must be globally unique
Duplicate identifier 'https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/IncompleteDevice' - identifiers must be globally unique (de.fraunhofer.iosb.ilt.faaast.service.starter.ExecutionExceptionHandler)
```
Your model contains a modeling error which can lead to severe issues.
If you wish to ignore this, you can start FA³ST Service with the –no-validation flag
```
java -jar starter-{version}.jar -m example.aasx –-no-validation
```

If the error is not specified, you are probably trying to load an older V2 model, such as provided by https://admin-shell-io.com/samples/
```
[ERROR] Error loading model file
```
In this case, the model has to be updated to V3 with the current version of AASX Package Explorer. If the V3 model can be loaded by the AASX Package Explorer and fails to load, please submit an issue with the model here: https://github.com/FraunhoferIOSB/FAAAST-Service/issues/new/choose

For testing purposes, we provide an example model here: https://github.com/FraunhoferIOSB/FAAAST-Service/tree/main/misc/examples
### Resource not found '/shells'
```
{"messages": [{
"messageType": "Error",
"text": "Resource not found '/shells'",
"code": "",
"timestamp": "2024-08-09T10:43:16.913+00:00"
}]}
```
If you use the API and get a "Resource not found" message, FA³ST Service could not find an appropriate API call for your request.
In many cases, providing the proper API prefix, for example <mark>/api/v3.0</mark> and following the up-to-date SwaggerHub API, should lead to a valid result:
https://faaast-service-v1.k8s.ilt-dmz.iosb.fraunhofer.de/api/v3.0/shells
Keep in mind that the right HTTP method must be selected for specific calls.

### Configuration could not be loaded

The most frequent issue with configuration files are inproper AAS references in the Asset Connection.
For example, to connect the operation "calculate" to the asset where the calculation is done, the reference "(Submodel)https://example.com/ids/sm/7230_2111_9032_0866, (Operation)calculate" is used.
It is important to have the whitespace between element and submodel and follow the exact AAS elements like "Property" or "File"
Example: 
```
"assetConnections": [
		{
			"@class": "de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.HttpAssetConnection",
			"baseUrl": "http://localhost:5001",
			"operationProviders":
			{
				"(Submodel)https://example.com/ids/sm/7230_2111_9032_0866, (Operation)calculate":
				{
					"path": "/add",
					"format": "JSON",
					"template": "{\"data\":{\"input1\": ${input1}, \"input2\": ${input2}}}",
					"queries":
					{
						"result": "$.result"
					}
				}
			}
		}
	]
```

Additionally, it should be checked if JSON syntax errors are present, for example with https://jsonchecker.com/


### Certificate & SSL errors
By default, FA³ST Service will generate a SSL certificate if none is provided. Those are self-generated certificates and can lead to security warnings in browsers and connection failures in AAS Clients.
To turn off SSL, the environment variable sslEnabled can be used. It can also be supplied with the configuration JSON file in the endpoint configuration: https://faaast-service.readthedocs.io/en/latest/interfaces/endpoint.html#http

```
java -jar starter-{version}.jar -m example.aasx endpoints[0]_sslEnabled=false
```

This flag should only be used for testing purposes on local machines.