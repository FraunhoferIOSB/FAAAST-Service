# HTTP

The HTTP Endpoint allows accessing data and execute operations within the FA³ST Service via REST-API.
The HTTP Endpoint is based on the document [Details of the Asset Administration Shell - Part 2](https://www.plattform-i40.de/IP/Redaktion/EN/Downloads/Publikation/Details_of_the_Asset_Administration_Shell_Part2_V1.html), _Interoperability at Runtime –
Exchanging Information via Application
Programming Interfaces (Version 1.0RC02)_' , November 2021 and the OpenAPI documentation [DotAAS Part 2 | HTTP/REST | Entire Interface Collection](https://app.swaggerhub.com/apis/Plattform_i40/Entire-Interface-Collection/V1.0RC01), Apr, 26th 2022

For detailed information on the REST API see
[DotAAS Part 2 | HTTP/REST | Entire Interface Collection](https://app.swaggerhub.com/apis/Plattform_i40/Entire-Interface-Collection/V1.0RC01), Apr, 26th 2022

In order to use the HTTP Endpoint the configuration settings require to include an HTTP Endpoint configuration, like the one below:
```json
{
	"endpoints": [
		{
			"@class": "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint",
			"port": 8080
		}
	]
}
```

## HTTP Example

Sample HTTP Call for Operation _GetSubmodelElementByPath_
using the parameters
-   _submodelIdentifier_: https://acplt.org/Test_Submodel (must be base64URL-encoded)
-   _idShortPath_: ExampleRelationshipElement (must be URL-encoded)

using the query-parameters _level=deep_ and _content=normal_.

> To avoid problems with IRIs in URLs the identifiers shall be BASE64-URL-encoded before using them as parameters in the HTTP APIs. IdshortPaths are URL-encoded to handle including square brackets.

```sh
http://localhost:8080/submodels/aHR0cHM6Ly9hY3BsdC5vcmcvVGVzdF9TdWJtb2RlbA==/submodel/submodel-elements/ExampleRelationshipElement?level=deep&content=normal
```

## HTTP API

### Supported API calls

-   Asset Administration Shell Repository Interface
    -   ![GET](https://img.shields.io/badge/GET-blue) ![POST](https://img.shields.io/badge/POST-brightgreen) /shells
    -   ![GET](https://img.shields.io/badge/GET-blue) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red) /shells/{aasIdentifier}

-   Asset Administration Shell Interface
    -   ![GET]https://img.shields.io/badge/GET-blue ![PUT](https://img.shields.io/badge/PUT-orange) /shells/{aasIdentifier}/aas
    -   ![GET]https://img.shields.io/badge/GET-blue ![PUT](https://img.shields.io/badge/PUT-orange) /shells/{aasIdentifier}/aas/asset-information
    -   ![GET]https://img.shields.io/badge/GET-blue ![POST](https://img.shields.io/badge/POST-brightgreen) /shells/{aasIdentifier}/aas/submodels
    -   ![DELETE](https://img.shields.io/badge/DELETE-red) /shells/{aasIdentifier}/aas/submodels{submodeIdentifier}

-   Submodel Repository Interface
    -   ![GET]https://img.shields.io/badge/GET-blue ![POST](https://img.shields.io/badge/POST-brightgreen) /submodels
    -   ![GET]https://img.shields.io/badge/GET-blue ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red) /submodels/{submodelIdentifier}

-   Submodel Interface
    -   ![GET]https://img.shields.io/badge/GET-blue ![PUT](https://img.shields.io/badge/PUT-orange) /submodels/{submodelIdentifier}/submodel
    -   ![GET]https://img.shields.io/badge/GET-blue ![POST](https://img.shields.io/badge/POST-brightgreen) /submodels/{submodelIdentifier}/submodel/submodel-elements
    -   ![GET]https://img.shields.io/badge/GET-blue ![POST](https://img.shields.io/badge/POST-brightgreen) ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red) /submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}
    -   ![POST](https://img.shields.io/badge/POST-brightgreen) /submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/invoke
    -   ![GET]https://img.shields.io/badge/GET-blue /submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/operation-Results/{handle-Id}

-   Submodel Interface (combined with Asset Administration Shell Interface)
    -   ![GET]https://img.shields.io/badge/GET-blue ![PUT](https://img.shields.io/badge/PUT-orange) /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel
    -   ![GET]https://img.shields.io/badge/GET-blue ![POST](https://img.shields.io/badge/POST-brightgreen) /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements
    -   ![GET]https://img.shields.io/badge/GET-blue ![POST](https://img.shields.io/badge/POST-brightgreen), ![PUT](https://img.shields.io/badge/PUT-orange), ![DELETE](https://img.shields.io/badge/DELETE-red) /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}
    -   ![POST](https://img.shields.io/badge/POST-brightgreen) /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/invoke
    -   ![GET]https://img.shields.io/badge/GET-blue /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/operation-Results/{handle-Id}

-   Concept Description Repository Interface
    -   ![GET]https://img.shields.io/badge/GET-blue ![POST](https://img.shields.io/badge/POST-brightgreen) /concept-descriptions
    -   ![GET]https://img.shields.io/badge/GET-blue ![PUT](https://img.shields.io/badge/PUT-orange) ![DELETE](https://img.shields.io/badge/DELETE-red) /concept-descriptions/{cdIdentifier}

-   Asset Administration Shell Basic Discovery
    -   ![GET]https://img.shields.io/badge/GET-blue /lookup/shells
    -   ![GET]https://img.shields.io/badge/GET-blue ![POST](https://img.shields.io/badge/POST-brightgreen) ![DELETE](https://img.shields.io/badge/DELETE-red) /lookup/shells/{aasIdentifier}

-   Asset Administration Shell Serialization Interface
    -   ![GET]https://img.shields.io/badge/GET-blue /serialization

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
