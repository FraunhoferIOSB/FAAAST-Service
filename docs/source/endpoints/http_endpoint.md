# HTTP Endpoint

The HTTP Endpoint allows accessing data and execute operations within the FA³ST Service via REST-API.
The HTTP Endpoint is based on the document [Details of the Asset Administration Shell - Part 2](https://www.plattform-i40.de/IP/Redaktion/EN/Downloads/Publikation/Details_of_the_Asset_Administration_Shell_Part2_V1.html), _Interoperability at Runtime –
Exchanging Information via Application
Programming Interfaces (Version 1.0RC02)_' , November 2021 and the OpenAPI documentation [DotAAS Part 2 | HTTP/REST | Entire Interface Collection](https://app.swaggerhub.com/apis/Plattform_i40/Entire-API-Collection/V1.0RC02), Apr, 26th 2022

## Configuration Parameters

| Name | Allowed Value | Description |
|:--| -- | -- |
| port | Integer |  _optional_ The port to use, default: 8080 |
| corsEnabled | Boolean | _optional_ If Cross-Origin Resource Sharing (CORS) should be enabled, typically required if you want to access the REST interface from any machine other than the one running FA³ST Service, default: false |
| httpsEnabled | Boolean | _optional_ If true, the endpoint will only be available via HTTPS; if false, it will only be available via HTTP, default: false |
| certificate; | Object | _optional_  The certificate to use when `httpsEnabled` is true, if none is provided a self-signed certificate will be generated [See details](../../gettingstarted/configuration#providing-certificates-in-configuration) |

### Example

In order to use the HTTP Endpoint the configuration settings require to include an HTTP Endpoint configuration, like the one below:
```json
{
	"endpoints": [
		{
			"@class": "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint",
			"port": 8080,
			"corsEnabled": true,
			"httpsEnabled": true,
			"certificate": {
				"keyStoreType": "PKCS12",
				"keyStorePath": "C:\faaast\MyKeyStore.p12",
				"keyStorePassword": "changeit",
				"keyAlias": "server-key",
				"keyPassword": "changeit"
			}
		}
	]
}
```

## API

### Supported API calls

-   Asset Administration Shell Repository Interface
    -   /shells ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen)
    -   /shells/{aasIdentifier} ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)

-   Asset Administration Shell Interface
    -   /shells/{aasIdentifier}/aas ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange)
    -   /shells/{aasIdentifier}/aas/asset-information ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange)
    -   /shells/{aasIdentifier}/aas/submodels ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen)
    -   /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier} ![DELETE](https://img.shields.io/badge/DELETE-red)

-   Submodel Repository Interface
    -   /submodels ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen)
    -   /submodels/{submodelIdentifier} ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)

-   Submodel Interface
    -   /submodels/{submodelIdentifier}/submodel ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange)
    -   /submodels/{submodelIdentifier}/submodel/submodel-elements ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen)
    -   /submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath} ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)
    -   /submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/invoke ![POST](https://img.shields.io/badge/POST-brightgreen)
    -   /submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/operation-results/{handle-Id} ![GET](https://img.shields.io/badge/GET-blue)

-   Submodel Interface (combined with Asset Administration Shell Interface)
    -   /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange)
    -   /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen)
    -   /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath} ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen), ![PUT](https://img.shields.io/badge/PUT-orange), ![DELETE](https://img.shields.io/badge/DELETE-red)
    -   /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/invoke ![POST](https://img.shields.io/badge/POST-brightgreen)
    -   /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/operation-results/{handle-Id} ![GET](https://img.shields.io/badge/GET-blue)

-   Concept Description Repository Interface
    -   /concept-descriptions ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen)
    -   /concept-descriptions/{cdIdentifier} ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red)

-   Asset Administration Shell Basic Discovery
    -   /lookup/shells ![GET](https://img.shields.io/badge/GET-blue)
    -   /lookup/shells/{aasIdentifier} ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen) ![DELETE](https://img.shields.io/badge/DELETE-red)

-   Asset Administration Shell Serialization Interface
    -   /serialization ![GET](https://img.shields.io/badge/GET-blue)

### Optional query parameters

-   level=deep|core
-   content=normal|value|path
-   extent=WithoutBLOBValue/WithBLOBValue
-   InvokeOperation supports async=true/false

They are added to the URL as regular query params

```sh
http://url:port?level=deep&content=value
```

FA³ST Service currently supports only content=value and content=normal


### Unsupported API calls

-   Asset Administration Shell Registry Interface (out-of-scope)
-   Submodel Registry Interface (out-of-scope)
-   AASX File Server Interface (probably supported in future)
    -   /packages
    -   /packages/{packageId}

## Example

Sample HTTP Call for Operation _GetSubmodelElementByPath_
using the parameters
-   _submodelIdentifier_: https://acplt.org/Test_Submodel (must be base64URL-encoded)
-   _idShortPath_: ExampleRelationshipElement (must be URL-encoded)

using the query-parameters _level=deep_ and _content=normal_.

> To avoid problems with IRIs in URLs the identifiers shall be BASE64-URL-encoded before using them as parameters in the HTTP APIs. IdshortPaths are URL-encoded to handle including square brackets.

```sh
http://localhost:8080/submodels/aHR0cHM6Ly9hY3BsdC5vcmcvVGVzdF9TdWJtb2RlbA==/submodel/submodel-elements/ExampleRelationshipElement?level=deep&content=normal
```
