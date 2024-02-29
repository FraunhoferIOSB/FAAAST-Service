# Getting Started

## Architecture

FA³ST Service uses the concept of an open architecture. This means, it is designed to be easily extenadable and customizable. 

The main components of FA³ST Service are `AAS Model`, which basically is a representation of the meta model classes of the AAS such as *Asset Administration Shell*, *Submodel*, *SubmodelElement*, or *Property*, and `Core`, which implements all the processing logic. 
Besides those two central components, FA³ST Service offers multiple interfaces that each can have different and/or custom implementations. 
FA³ST Service already ships with a number of so-called *default implementations* of these interfaces depicted by the light-grey boxes to the left and right in the figure.

```{figure} ../images/architecture.png
:width: 800px
:align: center
High-Level Architecture of FA³ST Service.
```

The interfaces provide the following functionalities:

- `Endpoint`:           Communication with the DT from the outside
- `MessageBus`:         Communication & synchronization between FA³ST Service components
- `De-/Serializer`:     De-/Serialization of AAS models in from/to data formats
- `Persistence`:        Persistent storage of data (model + values)
- `FileStorage`:        Peristent storage of complementary files (e.g. PDF files linked from the AAS)
- `AssetConnection`:    Synchronization with underlying asset(s)

## Configuration

Configuration in FA³ST Service happens primamirly via a single JSON file.
However, it is also possible to override configuration properties through command-line arguments and environment variables, where command-line arguments have precedence over environment variables while both override properties defined in the configuration file.

The configuration file contains a `core` section as well as multiple sections telling FA³ST Service which implementations to use for the available interfaces and how to configure them.

```{code-block} json
:caption: Structure of the configuration file.
:lineno-start: 1
{
	"core" : { },              // core configuration not related to interfaces
	"endpoints" : [ ],         // [0..*] default: HTTP
	"persistence" : { },       // [0..1] default: in-memory
	"fileStorage" : {},        // [0..1] default: in-memory
	"messageBus" : { },        // [0..1] default: internal
	"assetConnections": [ ]    // [0..*] default: none
}
```

A configuration base is always based on the default configuration, meaning that it only needs to contain properties that differ from the default configuration.
For example, providing only the `core` section is a valid configuration and will contain default values for all other sections.
This is a common scenario if you want to quickly setup FA³ST Service for your first experiments.

```{code-block} json
:caption: Configuration file with only `core` section. All other sections will use default values.
:lineno-start: 1
{
	"core" : { 
		// custom core settings
	}
}
```

### Core Configuration

The `core` configuration block contains properties not related to the implementation of any interface.

:::{table} Configuration properties of `core` configuration section.
| Name                                         | Allowed Values | Description                                                     | Default Value                   |
| -------------------------------------------- | -------------- | --------------------------------------------------------------- | ------------------------------- |
| requestHandlerThreadPoolSize<br>*(optional)* | Integer        | Number of concurrent thread that can execute API requests       | 2                               |
| assetConnectionRetryInterval<br>*(optional)* | Long           | Interval in ms in which to retry establishing asset connections | 1000                            |
| validationOnLoad<br>*(optional)*             | Object         | Validation rules to use when loading the AAS model at startup   | all enabled                     |
| validationOnCreate<br>*(optional)*           | Object         | Validation rules to use when creating new elements via API      | constraints validation disabled |
| validationOnUpdate<br>*(optional)*           | Object         | Validation rules to use when updating elements via API          | constraints validation disabled |
:::

```{code-block} json
:caption: Example `core` configuration
:lineno-start: 1
{
	"core" : {
		"requestHandlerThreadPoolSize": 2,      
		"assetConnectionRetryInterval": 1000,   
		"validationOnLoad": {					
			"validateConstraints": true,        // currently ignored because AAS4J does not yet implement validation for AAS v3.0
			"idShortUniqueness": true,
			"identifierUniqueness": true
		},
		"validationOnCreate": {
			"validateConstraints": false,        // currently ignored because AAS4J does not yet implement validation for AAS v3.0
			"idShortUniqueness": true,
			"identifierUniqueness": true
		},
		"validationOnUpdate": {
			"validateConstraints": false,        // currently ignored because AAS4J does not yet implement validation for AAS v3.0
			"idShortUniqueness": true,
			"identifierUniqueness": true
		}
	},
	// ...
}
```

### Configuring Interface Implementations

For each interface in the architecture, you can choose one (or sometimes multiple) interface(s) to be used.
As every interface implementation may require different configuration properties which FA³ST does not know about (as the implementation may be developed by 3rd parties at any time), the configuration section for each interface implementation uses the following structure

```{code-block} json
:caption: Common structure for configuring an interface implementation.
:lineno-start: 1
{
	"@class" : "...",     // fully-qualified Java class name of the class implementing the interface
	// implementation-specific configuration properties
}
```

Which properties are available for each implementation should be documented, e.g., for all default implementations these properties are documented in the corresponding page of the documentation for each of the implementations.

The following shows an example of a configuration using and HTTP endpoint with port 8080.

```{code-block} json
:caption: Example configuration with HTTP endpoint using port 8080.
:lineno-start: 1
{
	"endpoints" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint",
		"port" : 8080
	},
	// ...
}
```

### Using 3rd Party Interface Implementations

For FA³ST to be able to load an implementation that is not pre-packaged with FA³ST, you need to put a JAR file containing the respective class in the same directory as the FA³ST Service JAR. 
Furthermore, all dependencies of that class need also be resolvable. 
This can be achieved by either packaging them into the same JAR (e.g. using the [Maven Shade Plugin](https://maven.apache.org/plugins/maven-shade-plugin/)) or manually providing the required JAR files alongside the implementation.


(providing-certificates-in-configuration)=
### Providing certificates in configuration

Multiple components of FA³ST Service make use of certificates, either by using them for their own services or by trusting the provided certificates.
The default way to exchange certificates in FA³ST Service is via [Java KeyStore](https://docs.oracle.com/javase/8/docs/api/java/security/KeyStore.html)s.
To simplify configuration, the same configuration object is re-used across different components, for example in the [HTTP Endpoint](#endpoint-http).
The structure of the certificate-related configuration object is explained in the following.

:::{table} Configuration properties of generic certificate section.
| Name                         | Allowed Values | Description                                                                                                              | Default Value                       |
| -----------------------------| -------------- | ------------------------------------------------------------------------------------------------------------------------ | ----------------------------------- |
| keyPassword                  | String         | The password for the key.<br>**Warning: may cause unexpected behavior if not set or set to empty string in some cases**  |                                     |
| keyStorePassword             | String         | The password for the key store                                                                                           |                                     |
| keyStorePath                 | String         | File containing the key store                                                                                            |                                     |
| keyAlias<br>*(optional)*     | String         | The alias to use, e.g. when loading a certificate and the key store contains multiple entries                            | null, i.e. first entry will be used |
| keyStoreType<br>*(optional)* | String         | Type of the KeyStore, e.g.  PKCS12 or JKS                                                                                | PKCS12                              |
:::

```{code-block} json
:caption: Example certificate information
:lineno-start: 1
{
	"keyStoreType": "PKCS12",
	"keyStorePath": "C:\faaast\MyKeyStore.p12",
	"keyStorePassword": "changeit",
	"keyAlias": "server-key",
	"keyPassword": "changeit"
}
```
