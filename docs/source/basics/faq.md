# Frequently Asked Questions

1. [FA³ST Service does not load the AASX/JSON model](#loading)
2. [Resource not found '/shells'](#resources)
3. [Configuration could not be loaded](#configuration)
4. [Certificate & SSL errors](#ssl)
5. [CORS](#cors)
6. [Security with Reverse Proxy - basic authentication](#security)

:::{admonition} FA³ST Service does not load the AASX/JSON model
:class: note
:name: loading
If you get a validation error like:

```
[ERROR] Model validation failed with the following error(s):
Found 2 violation(s):
Duplicate identifier 'https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/SpecificConditionsForUse' - identifiers must be globally unique
Duplicate identifier 'https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/IncompleteDevice' - identifiers must be globally unique (de.fraunhofer.iosb.ilt.faaast.service.starter.ExecutionExceptionHandler)
```

Your model contains a modeling error which can lead to severe issues.
If you wish to ignore this, you can start FA³ST Service with the `--no-validation` flag.

```
java -jar starter-{version}.jar -m example.aasx --no-validation
```

If the error is not specified, you are probably trying to load an older V2 model, such as provided by [admin-shell-io](https://admin-shell-io.com/samples/).

```
[ERROR] Error loading model file
```

In this case, the model has to be updated to V3 with the current version of AASX Package Explorer. If the V3 model can be loaded by the AASX Package Explorer and fails to load, please submit an issue with the model [here](https://github.com/FraunhoferIOSB/FAAAST-Service/issues/new/choose).

For testing purposes, we provide an example model here: <!--start:download-model-->
{download}`Model <https://github.com/FraunhoferIOSB/FAAAST-Service/tree/main/misc/examples>`<!--end:download-model-->

:::

:::{admonition} Resource not found '/shells'
:class: note
:name: resources

```{code-block} json
{
	"messages": [
		{
			"messageType": "Error",
			"text": "Resource not found '/shells'",
			"code": "",
			"timestamp": "2024-08-09T10:43:16.913+00:00"
		}
	]
}

```

If you use the API and get a "Resource not found" message, FA³ST Service could not find an appropriate API call for your request.
In many cases, providing the proper API prefix, for example `/api/v3.0` and following the up-to-date SwaggerHub API, should lead to a valid result:
`https://faaast-service-v1.k8s.ilt-dmz.iosb.fraunhofer.de/api/v3.0/shells`
Keep in mind that the right HTTP method must be selected for specific calls.
:::

:::{admonition} Configuration could not be loaded
:class: note
:name: configuration
The most frequent issue with configuration files are inproper AAS references in the Asset Connection.
For example, to connect the operation "calculate" to the asset where the calculation is done, the reference "(Submodel)https://example.com/ids/sm/7230_2111_9032_0866, (Operation)calculate" is used.
It is important to have the whitespace between element and submodel and follow the exact AAS elements like "Property" or "File"
Example:

```{code-block} json

{

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
	],
	// other configurations
}

```

Additionally, it should be checked if JSON syntax errors are present, for example with [jsonchecker](https://jsonchecker.com/)
:::

:::{admonition} Certificate & SSL errors
:class: note
:name: ssl
By default, FA³ST Service will generate a SSL certificate if none is provided. Those are self-generated certificates and can lead to security warnings in browsers and connection failures in AAS Clients.
To turn off SSL, the environment variable sslEnabled can be used. It can also be supplied with the configuration JSON file in the [endpoint configuration](https://faaast-service.readthedocs.io/en/latest/interfaces/endpoint.html#http).

```{code-block} console
java -jar starter-{version}.jar -m example.aasx endpoints[0]_sslEnabled=false
```

This flag should only be used for testing purposes on local machines. For public services, provide valid certificates via the configuration file.
:::

:::{admonition} CORS
:class: note
:name: cors
Another common issue when accessing FA³ST Service is a cross-origin resource sharing block.
By default, the HTTP endpoint does not enable CORS, but it typically is required when you want to access the REST interface from any machine other than the one running FA³ST Service.
The flag can be set with the [HTTP endpoint configuration](https://faaast-service.readthedocs.io/en/latest/interfaces/endpoint.html#http) or via command-line:

```{code-block} console
java -jar starter-{version}.jar -m example.aasx endpoints[0]_corsEnabled=true
```

:::

:::{admonition} Security with Reverse Proxy - basic authentication
:class: hint
:name: security
As AAS specification Part 4 Security is work-in-progress, to protect public services against unauthorized requests, basic authentication via reverse proxy can be configured.
For NGINX, detailed information can be found [here](https://kubernetes.github.io/ingress-nginx/examples/auth/basic/).

An example configuration with secret <b>basic-auth-secret</b>:

```{code-block} yaml
metadata:
name: ingress-with-auth
annotations:
nginx.ingress.kubernetes.io/auth-type: basic
nginx.ingress.kubernetes.io/auth-secret: basic-auth-secret
nginx.ingress.kubernetes.io/auth-realm: 'Authentication Required - FA³ST'
```

The authentication configuration will vary based on your deployment environment.
:::
