# SubmodelTemplate Processors

Specific SubmodelTemplates require special handling. SubmodelTemplate Processors provide the necessary functionality offered by these SubmodelTemplates.

## Asset Interfaces Description and Asset Interfaces Mapping Configuration

The SubmodelTemplate "Asset Interfaces Description" (AID) specifies an information model and a common representation for describing the interfaces of an asset service or asset related service. Based on this information, it is possible to initiate a connection to such a service and request or subscribe to served datapoints.
The Asset Interfaces Description (AID) in version 1.0 supports the description of interfaces based on three specific protocols: Modbus, HTTP and MQTT. Fa³st currently supports HTTP and MQTT, Modbus is currently not supported.

The SubmodelTemplate "Asset Interfaces Mapping Configuration" (AIMC) specifies an information model and a common representation for describing the mapping of interface(s) of an asset service or asset-related service already described in an Asset Interfaces Description (AID) Submodel. It can be understood as a configuration Submodel for south-bound communication between AAS and asset. Based on this information, it's possible to create an AssetConnection and map the payloads to the intended locations in an AAS automatically.

This SubmodelTemplate Processor uses these SubmodelTemplates to create AssetConnections from the given data and links the asset data to the corresponding SubmodelElements, as defined in AIMC.

### Configuration

The processor uses the following configuration structure:

```{code-block} json
:caption: Configuration structure for AID + AIMC SubmodelTemplate Processor.
:lineno-start: 1
{
    "submodelTemplateProcessors": [
        {
            "@class": "de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.AimcSubmodelTemplateProcessor",
            "interfaceConfigurations": {
                "{serialized Reference of AID Interface element}": {
                    // Interface configuration
                }
            }
        }
    ]
}
```

The value of `{serialized Reference of AID Interface element}` is the Reference to the SubmodelElementCollection "Interface" (SemanticId [https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Interface](https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Interface)) serialized using the rules described in [Section 7.2.3 of AAS Specification - Part 1](https://industrialdigitaltwin.org/wp-content/uploads/2023/06/IDTA-01001-3-0_SpecificationAssetAdministrationShell_Part1_Metamodel.pdf).
An example value could look like this `[ModelRef](Submodel)urn:aas:id:example:submodel:assetinterfacesdescription1, (SubmodelElementCollection)InterfaceHTTP`.

Add an Interface configuration section for each Asset Interface where additional configuration information is necessary.

:::{important}
The format for serializing references has changed with AAS v3.0 resp. FA³ST Service v1.0. For example, the id type is now no longer part of the serialization and path elements are now separated by `, ` (comma followed by space) instead of `,` (comma).
:::

#### Interface configuration

:::{table} Configuration properties of Interface configuration.
| Name                                | Allowed Value                                               | Description                                                                                    | Default Value |
| ----------------------------------- | ----------------------------------------------------------- |----------------------------------------------------------------------------------------------- | ------------- |
| password<br>*(optional)*            | String                                                      | Password for connecting to the Asset server.                                                    |               |
| username<br>*(optional)*            | String                                                      | Username for connecting to the Asset server.                                                    |               |
| subscriptionInterval<br>*(optional)*| long               | Interval to poll the server for changes (in ms).                                                                                                | 100           |
:::


```{code-block} json
:caption: Example configuration section for AID + AIMC SubmodelTemplate Processor.
:lineno-start: 1
{
	"@class": "de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.AimcSubmodelTemplateProcessor",
	"interfaceConfigurations": {
		"[ModelRef](Submodel)https://example.com/ids/sm/AssetInterfacesDescription, (SubmodelElementCollection)InterfaceHTTP": {
			"username": "user",
			"password": "password",
			"subscriptionInterval": 500
		}
	}
}
```
