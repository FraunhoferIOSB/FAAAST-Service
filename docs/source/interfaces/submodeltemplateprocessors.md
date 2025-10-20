# SubmodelTemplate Processors

Specific SubmodelTemplates require special handling. SubmodelTemplate Processors provide the necessary functionality offered by these SubmodelTemplates.

## Interfaces

Each SubmodelTemplate Processor must implement the interface SubmodelTemplateProcessor, which contains 4 methods:

```{code-block} Java
    public boolean accept(Submodel submodel);
    public boolean add(Submodel submodel, AssetConnectionManager assetConnectionManager);
    public boolean update(Submodel submodel, AssetConnectionManager assetConnectionManager);
    public boolean delete(Submodel submodel, AssetConnectionManager assetConnectionManager);
```

- "accept" is used to find out whether the given SubmodelTemplate Processor uses the specified Submodel.
- "add" is called from the Service if the given Submodel was added.
- "update" is called from the Service if the given Submodel was updated.
- "delete" is called from the Service if the given Submodel was deleted.

## Asset Interfaces Description and Asset Interfaces Mapping Configuration

The SubmodelTemplate [Asset Interfaces Description](https://industrialdigitaltwin.org/wp-content/uploads/2024/01/IDTA-02017-1-0_Submodel_Asset-Interfaces-Description.pdf) (AID) specifies an information model and a common representation for describing the interfaces of an asset service or asset related service. Based on this information, it is possible to initiate a connection to such a service and request or subscribe to served datapoints.
The Asset Interfaces Description (AID) in version 1.0 supports the description of interfaces based on three specific protocols: Modbus, HTTP and MQTT. Fa³st currently supports HTTP and MQTT, Modbus is currently not supported.

The SubmodelTemplate [Asset Interfaces Mapping Configuration](https://industrialdigitaltwin.org/wp-content/uploads/2024/06/IDTA-02027-1-0_Submodel_AssetInterfacesMappingConfiguration.pdf) (AIMC) specifies an information model and a common representation for describing the mapping of interface(s) of an asset service or asset-related service already described in an Asset Interfaces Description (AID) Submodel. It can be understood as a configuration Submodel for south-bound communication between AAS and asset. Based on this information, it's possible to create an [AssetConnection](#assetconnection) and map the payloads to the intended locations in an AAS automatically.

This SubmodelTemplate Processor uses these SubmodelTemplates to create AssetConnections from the given data and links the asset data to the corresponding SubmodelElements, as defined in AIMC.

The Processor looks for the SubmodelTemplate AIMC and maps all relations in the supported Asset Interfaces.

### Generic Configuration

The processor uses the following configuration structure:

```{code-block} json
:caption: Configuration structure for AID + AIMC SubmodelTemplate Processor.
:lineno-start: 1
{
    "submodelTemplateProcessors": [
        {
            "@class": "de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.AimcSubmodelTemplateProcessor",
            "credentials": {
                "{Server URL}": [
                    // credential configuration
                ]
            }
        }
    ]
}
```

The value of `{Server URL}` is the URL of the Server. This must match the Base URL in the AID Submodel.
An example value could look like this `http://myserver.example.com:8088`.

Add a list of Credentials for each Server URL.

### Credential configuration

:::{table} Configuration properties of Credential configuration.
| Name                                | Allowed Value                                               | Description                                                                                    | Default Value |
| ----------------------------------- | ----------------------------------------------------------- |----------------------------------------------------------------------------------------------- | ------------- |
| password            | String                                                      | Password for connecting to the Asset server.                                                    |               |
| username            | String                                                      | Username for connecting to the Asset server.                                                    |               |
:::

In the EndpointMetadata of AID the following attributes are currently evaluated:

- base
- contentType
- security

Currently, the supported Security Schemes are:

- NoSecurityScheme (nosec_sc)
- BasicSecurityScheme (basic_sc)

If BasicSecurityScheme is configured, username and password from the SMT configuration is used. In that case, make sure, that valid username and password is configured.

In the Property of AID the following attributes are currently evaluated:

- observable (only HTTP)

For HTTP: If observable is true, a SubscriptionProvider, otherwise a ValueProvider is created.
In case of MQTT, a SubscriptionProvider is created always.

The subscriptionInterval for a SubscriptionProvider is taken from the corresponding configuration section of the Processor (if available). If no value is provided, the default value will be used.

In the Property Forms of AID, the following attributes are currently evaluated:

- href
- contentType
- htv_headers (for HTTP)

:::{table} Overview of the mappings of the used AID attributes.
| AID Attribute                       | Value in Asset Connection HTTP            | Value in Asset Connection MQTT                              |
| ----------------------------------- | ----------------------------------------- |------------------------------------------------------------ |
| base (EndpointMetadata)             | baseUrl                                   | serverUri                                                   |
| contentType                         | format                                    | format                                                      |
| href                                | path                                      | topic                                                       |
| htv_headers                         | headers                                   | -                                                           |
:::

```{code-block} json
:caption: Example configuration section for AID + AIMC SubmodelTemplate Processor.
:lineno-start: 1
{
    "@class": "de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.AimcSubmodelTemplateProcessor",
    "credentials": {
        "http://myserver.example.com:8088": [
            {
                "username": "user1",
                "password": "pw1"
            },
            {
                "username": "user2",
                "password": "pw2"
            }
        ]
    }
}
```
