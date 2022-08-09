# HTTP-Endpoint Interface

The HTTP-Endpoint allows accessing data and execute operations within the FA³ST-Service via REST-API.
The HTTP-Endpoint Interface is based on the document [Details of the Asset Administration Shell - Part 2](https://www.plattform-i40.de/IP/Redaktion/EN/Downloads/Publikation/Details_of_the_Asset_Administration_Shell_Part2_V1.html), _Interoperability at Runtime –
Exchanging Information via Application
Programming Interfaces (Version 1.0RC02)_' , November 2021 and the OpenAPI documentation [DotAAS Part 2 | HTTP/REST | Entire Interface Collection](https://app.swaggerhub.com/apis/Plattform_i40/Entire-Interface-Collection/V1.0RC01), Apr, 26th 2022

For detailed information on the REST API see
[DotAAS Part 2 | HTTP/REST | Entire Interface Collection](https://app.swaggerhub.com/apis/Plattform_i40/Entire-Interface-Collection/V1.0RC01), Apr, 26th 2022

In order to use the HTTP-Endpoint the configuration settings require to include an HTTP-Endpoint configuration, like the one below:
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

Sample HTTP-Call for Operation _GetSubmodelElementByPath_
using the parameters
-   _submodelIdentifier_: https://acplt.org/Test_Submodel (must be base64URL-encoded)
-   _idShortPath_: ExampleRelationshipElement (must be URL-encoded)

using the query-parameters _level=deep_ and _content=normal_.

> To avoid problems with IRIs in URLs the identifiers shall be BASE64-URL-encoded before using them as parameters in the HTTP-APIs. IdshortPaths are URL-encoded to handle including square brackets.

```sh
http://localhost:8080/submodels/aHR0cHM6Ly9hY3BsdC5vcmcvVGVzdF9TdWJtb2RlbA==/submodel/submodel-elements/ExampleRelationshipElement?level=deep&content=normal
```

Returns a specific submodel element from the Submodel at a specified path

## HTTP-API

### The following interface URLs are fully supported

-   Asset Administration Shell Repository Interface
    -   /shells (GET, POST)
    -   /shells/{aasIdentifier} (GET, PUT, DELETE)

-   Asset Administration Shell Interface
    -   /shells/{aasIdentifier}/aas (GET, PUT)
    -   /shells/{aasIdentifier}/aas/asset-information (GET, PUT)
    -   /shells/{aasIdentifier}/aas/submodels (GET,POST)
    -   /shells/{aasIdentifier}/aas/submodels{submodeIdentifier} (DELETE)

-   Submodel Repository Interface
    -   /submodels (GET, POST)
    -   /submodels/{submodelIdentifier} (GET, PUT, DELETE)

-   Submodel Interface
    -   /submodels/{submodelIdentifier}/submodel (GET, PUT)
    -   /submodels/{submodelIdentifier}/submodel/submodel-elements (POST)
    -   /submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath} (GET, POST, PUT, DELETE)
    -   /submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/invoke (POST)
    -   /submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/operation-Results/{handle-Id} (GET)

-   Submodel Interface (combined with Asset Administration Shell Interface)
    -   /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel (GET, PUT)
    -   /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements (POST)
    -   /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath} (GET, POST, PUT, DELETE)
    -   /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/invoke (POST)
    -   /shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/operation-Results/{handle-Id} (GET)

-   Concept Description Repository Interface
    -   concept-descriptions (GET, POST)
    -   concept-descriptions/{cdIdentifier} (GET, PUT, DELETE)

### Optional query params are

-   level=deep/core
-   content=normal/trimmed/value
-   extent=WithoutBLOBValue/WithBLOBValue
-   InvokeOperation supports async=true/false

They are added to the URL as regular query params

```sh
http://url:port?level=deep&content=value
```

FA³ST Service currently supports only content=value and content=normal


### The following interface URLs are currently not (yet) supported

-   Asset Administration Shell Registry Interface (not in Scope of FA³ST-Service)
-   Submodel Registry Interface (not in Scope of FA³ST-Service)
-   AASX File Server Interface (not yet supported)
    -   /packages
    -   /packages/{packageId}
-   Asset Administration Shell Serialization Interface (not yet supported)
    -   /serialization (GET)
-   Asset Administration Shell Basic Discovery (not yet supported)
    -   /lookup/shells
    -   /lookup/shells/{aasIdentifier}
