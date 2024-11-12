# AssetConnection

The AssetConnection interface is responsible for synchronizing values of the model with assets.
Although asset synchronization is not part of the AAS specification, we believe this functionality is essential for digital twins, at least when located on edge-level, i.e., close to an actual machine/asset.

The following figure depicts how asset synchronization works in more detail.

```{figure} ../images/asset-connection.png
:width: 600px
:align: center
How AssetConnection works in FA³ST Service.
```

The top half shows a examplenary AAS model that we want to synchronize with the underlying asset.
In the center we have the AssetConnection interface which holds multiple of so-called *Provider*s.
There are three types of providers:

- ValueProvider: for reading data from and writing data to to asset whenever the value of the corresponding AAS element is read/written
- OperationProvider: for forwarding operation invocation requests to the asset and translating the response back to be AAS-compliant
- SubscriptionProvider: for subscribing to changes on the asset and therefore continuously updating the value of the corresponding AAS element

The mapping between AAS elements and providers is defined in the configuration of the AssetConnection.
Therefore, the configuration section for all implementations of the AssetConnection interface share the following common structure.

```{code-block} json
:caption: Common configuration structure for all AssetConnection implementations.
:lineno-start: 1
{
	"assetConnections": [ {
		"@class": "...",
		// connection-level configuration
		"valueProviders":
		{
			"{serialized Reference of AAS element}":
			{
				// value provider configuration
			}
		},
		"operationProviders":
		{
			"{serialized Reference of AAS element}":
			{
				// operation provider configuration
			}
		},
		"subscriptionProviders":
		{
			"{serialized Reference of AAS element}":
			{
				// subscription provider configuration
			}
		}
	}],
	//...
}
```

The value of `{serialized Reference of AAS element}` is the Reference to the AAS element serialized using the rules described in [Section 7.2.3 of AAS Specification - Part 1](https://industrialdigitaltwin.org/wp-content/uploads/2023/06/IDTA-01001-3-0_SpecificationAssetAdministrationShell_Part1_Metamodel.pdf).
An example value could look like this `[ModelRef](Submodel)urn:aas:id:example:submodel:1, (Property)Property1`.

:::{important}
The format for serializing references has changed with AAS v3.0 resp. FA³ST Service v1.0. For example, the id type is now no longer part of the serialization and path elements are now separated by `, ` (comma followed by space) instead of `,` (comma).
:::

The available configuration properties for connection-level and the providers are implementation-specific.
This is necessary because different protocols require different types of information, e.g. for OPC UA an AAS element could be mapped to an OPC UA node which means the configuration must contain the node ID, while for MQTT we need a topic on which to listen and maybe even information about the payload format.

:::{note}
An implementation does not have to implement all three provider types. In fact, it is often not possible to implement all of them for a given network protocol as most protocols do not support pull-based and pub/sub mechanisms at the same time (e.g. HTTP, MQTT).
:::

:::{tip}
You can define both a ValueProvider and a SubscriptionProvider for the same element. This allows you to reflect in the asset changes in near real-time in your AAS and at the same time to update the value on the asset via the AAS API. This is especially useful when starting FA³ST with an OPC UA endpoint as it allows users to subscribe to changes or AAS properties via OPC UA.
:::


## OperationProvider Configuration
All OperationProvider share the following common set of configuration properties.

:::{table} Common configuration properties of OperationProviders.
| Name                                   | Allowed Value                                               | Description                             | Default Value             |
| -------------------------------------- | ----------------------------------------------------------- |---------------------------------------- | ------------------------- |
| inputValidationMode<br>*(optional)*    | NONE<br>REQUIRE_PRESENT<br>REQUIRE_PRESENT_OR_DEFAULT       | Validation mode for input arguments     | REQUIRE_PRESENT_OR_DEFAULT|
| inoutputValidationMode<br>*(optional)* | NONE<br>REQUIRE_PRESENT<br>REQUIRE_PRESENT_OR_DEFAULT       | Validation mode for inoutput arguments  | REQUIRE_PRESENT_OR_DEFAULT|
| outputValidationMode<br>*(optional)*   | NONE<br>REQUIRE_PRESENT<br>REQUIRE_PRESENT_OR_DEFAULT       | Validation mode for ouput arguments     | REQUIRE_PRESENT_OR_DEFAULT|
:::

### Validation of operation arguments
Validation of operation argument can be configured independently for in-, out-, and inoutput arguments to be one of the following values
- NONE: no validation at all is performed
- REQUIRE_PRESENT: requires all arguments defined for the operation to be provided in the call and all arguments provided to be defined for the operation. This check works only on argument name (idShort) and not argument datatype.
- REQUIRE_PRESENT_OR_DEFAULT: sets all arguments defined for the operation but not provided in the call to the default value, i.e. the value given in the definition of the argument. Similar to REQUIRE_PRESENT, this requires the call to only contain arguments that are defined for the operation and works only on argument name ignoring the argument datatype.

## HTTP

### Supported Providers

- ValueProvider
	- read ✔️
	- write ✔️
- OperationProvider ✔️
- SubscriptionProvider ✔️ (via polling)

### Configuration

#### Connection-Level

:::{table} Configuration properties of HTTP AssetConnection.
| Name                                | Allowed Value                                               | Description                                                                                    | Default Value |
| ----------------------------------- | ----------------------------------------------------------- |----------------------------------------------------------------------------------------------- | ------------- |
| baseUrl                             | String                                                      | Base URL of the HTTP server, e.g. *http://example.com*.                                        |               |
| headers<br>*(optional)*             | Map<String,String>                                          | Headers to send with each request.                                                             | *empty list*  |
| password<br>*(optional)*            | String                                                      | Password for connecting to the HTTP server.                                                    |               |
| trustedCertificates<br>*(optional)* | [CertificateInfo](#providing-certificates-in-configuration) | Trusted certificates, i.e. when connecting to a server that is using self-signed certificates. |               |   
| username<br>*(optional)*            | String                                                      | Username for connecting to the HTTP server.                                                    |               |
:::

#### Value Provider

:::{table} Configuration properties of HTTP AssetConnection Value Provider.
| Name                        | Allowed Value      | Description                                                                                                                                     | Default Value |
| --------------------------- | ------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------- | ------------- |
| format                      | JSON<br>XML        | Content format of the payload.                                                                                                                  |               |
| headers<br>*(optional)*     | Map<String,String> | Headers to send with each request.<br>Overrides connection-level headers.                                                                       | *empty list*  |
| path                        | String             | Path for the HTTP request, relative to the `baseUrl` of the connection.                                                                         |               |
| query<br>*(optional)*       | String             | Additional information how to extract actual value from received messages.<br>Depends on `format`, e.g. for JSON this is a JSONPath expression. |               |
| template<br>*(optional)*    | String             | Template used to format payload when sending via HTTP.                                                                                          |               |
| writeMethod<br>*(optional)* | GET<br>PUT<br>POST | HTTP method to use when writing a value to HTTP.                                                                                                | PUT           |
:::

```{code-block} json
:caption: Example configuration section for HTTP AssetConnection.
:lineno-start: 1
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

#### Operation Provider

:::{table} Configuration properties of HTTP AssetConnection Operation Provider.
| Name                                   | Allowed Value                                         | Description                                                                                                                                                                                 | Default Value             |
| -------------------------------------- | ----------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------- |
| format                                 | JSON<br>XML                                           | Content format of the payload.                                                                                                                                                              |                           |
| headers<br>*(optional)*                | Map<String,String>                                    | Headers to send with each request.<br>Overrides connection-level headers.                                                                                                                   | *empty list*              |
| inputValidationMode<br>*(optional)*    | NONE<br>REQUIRE_PRESENT<br>REQUIRE_PRESENT_OR_DEFAULT | Validation mode for input arguments                                                                                                                                                         | REQUIRE_PRESENT_OR_DEFAULT|
| inoutputValidationMode<br>*(optional)* | NONE<br>REQUIRE_PRESENT<br>REQUIRE_PRESENT_OR_DEFAULT | Validation mode for inoutput arguments                                                                                                                                                      | REQUIRE_PRESENT_OR_DEFAULT|
| method<br>*(optional)*                 | PUT<br>POST                                           | HTTP method to use.                                                                                                                                                                         | POST                      |
| outputValidationMode<br>*(optional)*   | NONE<br>REQUIRE_PRESENT<br>REQUIRE_PRESENT_OR_DEFAULT | Validation mode for ouput arguments                                                                                                                                                         | REQUIRE_PRESENT_OR_DEFAULT|
| path                                   | String                                                | Path for the HTTP request, relative to the `baseUrl` of the connection.                                                                                                                     |                           |
| queries<br>*(optional)*                | Map<String,String>                                    | Map of result variable idShorts and corresponding query expressions to fetch them from returned value<br>Query expressions depend on `format`, e.g. for JSON this is a JSONPath expression. |                           |
| template<br>*(optional)*               | String                                                | Template used to format payload when sending via HTTP.                                                                                                                                      |                           |
:::



```{code-block} json
:caption: Example configuration section for HTTP OperationProvider for an Operation with input parameters `in1` and `in2` and output parameters `out1` and `out2`.
:lineno-start: 1
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

#### Subscription Provider

:::{table} Configuration properties of HTTP AssetConnection Subscription Provider.
| Name                        | Allowed Value      | Description                                                                                                                                     | Default Value |
| --------------------------- | ------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------- | ------------- |
| format                      | JSON<br>XML        | Content format of the payload.                                                                                                                  |               |
| headers<br>*(optional)*     | Map<String,String> | Headers to send with each request.<br>Overrides connection-level headers.                                                                       | *empty list*  |
| interval<br>*(optional)*    | long               | Interval to poll the server for changes (in ms).                                                                                                | 100           |
| path                        | String             | Path for the HTTP request, relative to the `baseUrl` of the connection.                                                                         |               |
| query<br>*(optional)*       | String             | Additional information how to extract actual value from received messages.<br>Depends on `format`, e.g. for JSON this is a JSONPath expression. |               |
:::


```{code-block} json
:caption: Example configuration section for HTTP SubscriptionProvider.
:lineno-start: 1
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

## MQTT

### Supported Providers

- ValueProvider
	- read ❌
	- write ✔️
- OperationProvider ❌
- SubscriptionProvider ✔️

### Configuration

#### Connection-Level

:::{table} Configuration properties of MQTT AssetConnection.
| Name                     | Allowed Value | Description                                         | Default Value        |
| ------------------------ | ------------- | --------------------------------------------------- | -------------------- |
| clientId<br>*(optional)* | String        | Id of the MQTT client used to connect to the server | *randomly generated* |
| password<br>*(optional)* | String        | Password for connecting to the MQTT server          |                      |
| serverUri                | String        | URL of the MQTT server, e.g. *tcp://localhost:1883* |                      |
| username<br>*(optional)* | String        | Username for connecting to the MQTT server          |                      |
:::

#### Value Provider

:::{table} Configuration properties of MQTT AssetConnection Value Provider.
| Name                     | Allowed Value | Description                      | Default Value        |
| ------------------------ | ------------- | -------------------------------- | -------------------- |
| format                   | JSON<br>XML   | Content format of the payload.   |                      |
| topic                    | String        | MQTT topic to use.               |                      |
| template<br>*(optional)* | String        | Template used to format payload. |                      |
:::

```{code-block} json
:caption: Example configuration section for MQTT ValueProvider.
:lineno-start: 1
{
	"format": "JSON",
	"topic": "example/myTopic",
	"template": "{\"foo\" : \"${value}\"}"
}
```

#### Subscription Provider

:::{table} Configuration properties of MQTT AssetConnection Subscription Provider.
| Name                  | Allowed Value | Description                                                                                                                                     | Default Value        |
| --------------------- | ------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- | -------------------- |
| format                | JSON<br>XML   | Content format of the payload.                                                                                                                  |                      |
| topic                 | String        | MQTT topic to use.                                                                                                                              |                      |
| query<br>*(optional)* | String        | Additional information how to extract actual value from received messages.<br>Depends on `format`, e.g. for JSON this is a JSONPath expression. |                      |
:::

```{code-block} json
:caption: Example configuration section for MQTT SubscriptionProvider.
:lineno-start: 1
{
	"format": "JSON",
	"topic": "example/myTopic",
	"query": "$.foo"
}
```

## OPC UA

### Supported Providers

- ValueProvider
	- read ✔️
	- write ✔️
- OperationProvider ✔️
- SubscriptionProvider ✔️

### Configuration

#### Connection-Level

:::{table} Configuration properties of OPC UA AssetConnection.
| Name                                      | Allowed Value                                                                                         | Description                                                                                                      | Default Value     |
| ----------------------------------------- | ----------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------- | ----------------- |
| acknowledgeTimeout<br>*(optional)*        | Integer                                                                                               | Timeout for acknowledgement (in ms).                                                                             | 10000             |
| applicationCertificate<br>*(optional)*    | [CertificateInfo](#providing-certificates-in-configuration)                                           | The application certificate.                                                                                     |                   |
| authenticationCertificate<br>*(optional)* | [CertificateInfo](#providing-certificates-in-configuration)                                           | The authentication/user certificate.                                                                             |                   |
| host                                      | String                                                                                                | URL of the OPC UA server, e.g. *opc.tcp://localhost:4840*                                                        |                   |
| password<br>*(optional)*                  | String                                                                                                | Password for connecting to the OPC UA server.<br>This value is required if `userTokenType` is set to `UserName`. |                   |
| requestTimeout<br>*(optional)*            | int                                                                                                   | Timeout for requests (in ms)                                                                                     | 3000              |
| securityBaseDir<br>*(optional)*           | String                                                                                                | Base directory for the certificate handling.                                                                     | .                 |
| securityMode<br>*(optional)*              | None<br>Sign<br>SignAndEncrypt                                                                        | Security Mode for the connection to the OPC UA server.                                                           | None              |
| securityPolicy<br>*(optional)*            | None<br>Basic256Sha256<br>Aes128_Sha256_RsaOaep<br>Aes256_Sha256_RsaPss                               | Desired Security Policy for the connection to the OPC UA server.                                                 | None              |
| transportProfile<br>*(optional)*          | TCP_UASC_UABINARY<br>HTTPS_UABINARY<br>HTTPS_UAXML<br>HTTPS_UAJSON<br>WSS_UASC_UABINARY<br>WSS_UAJSON | Transport Profile for the connection to the OPC UA server.                                                       | TCP_UASC_UABINARY |
| username<br>*(optional)*                  | String                                                                                                | Username for connecting to the OPC UA server.<br>This value is required if `userTokenType` is set to `UserName`. |                   |
| userTokenType<br>*(optional)*             | Anonymous<br>UserName<br>Certificate                                                                  | User Token Type for connecting to the OPC UA server.                                                             | Anonymous         |
:::

##### Remarks on certificate management
In OPC UA , certificates can be used for two purposes:
- encryption & signing of messages, and
- authentication of a client.

We call the certificate used for encryption _application certificate_ and the one used for authenticating a client _authentication certificate_.
You can choose to use only one of these options or both.
If using both, you can use different or the same certificates.

###### Application Certificate
An application certificate is required if the property `securityMode` is set to `Sign` or `SignAndEncrypt`.

Which application certificate to use is determined by the following steps:
- `applicationCertificate.keyStorePath` if it is an absolute file path and the file exists (default: application.p12)
- `{securityBaseDir}/{applicationCertificate.keyStorePath}` if the file exists (default: `./{applicationCertificate.keyStorePath}`)
- otherwise generate self-signed certificate and store it at `applicationCertificate.keyStorePath` (if `applicationCertificate.keyStorePath` is an absolute file path) or else `{securityBaseDir}/{applicationCertificate.keyStorePath}`. The generated keystore will not be password protected.

You also need to make sure that the OPC UA client (which in this case is the FA³ST Service OPC UA asset connection) knows and trusts the server certificate and vice versa.

For the client to trust the server you need to do one of these steps depending on the certificate of the server:
- Self-signed-certificate: Put server certificate in {securityBaseDir}/pki/trusted/certs
- CA Certificate: put the CA root certificate in {securityBaseDir}/pki/issuers/certs and the corresponding certificate revocation list (CRL) in {securityBaseDir}/pki/issuers/crl.

If you don't have the server certificate at hand you can start FA³ST Service without providing/trusting the server certificate.
On start-up FA³ST Service will try to connect to the server which will fail because the server certificate is not trusted yet.
After that you will find the relevant files at `{securityBaseDir}/pki/rejected`.
Copy them to the respective directories as described above.
Once FA³ST Service tries to reconnect the connection should be established successfully.

For the server to trust your client application certificate please refer to the documentation of your OPC UA server.

###### Authentication Certificate
Which authentication certificate is used is determined by a similar logic as for the application certificate besides that this certificate is not auto-generated if not present:
- `authenticationCertificate.keyStorePath` if it is an absolute file path and the file exists (default: application.p12)
- `{securityBaseDir}/{authenticationCertificate.keyStorePath}` if the file exists (default: `./{authenticationCertificate.keyStorePath}`)


#### Value Provider

:::{table} Configuration properties of OPC UA AssetConnection Value Provider.
| Name                       | Allowed Value | Description                                                                                                                             | Default Value        |
| -------------------------- | ------------- | --------------------------------------------------------------------------------------------------------------------------------------- | -------------------- |
| arrayIndex<br>*(optional)* | String        | Index of the desired array element if the node is an array.<br>Can be multi-dimensional.                                                |                      |
| nodeId                     | String        | NodeId of the the OPC UA node to read/write in [ExpandedNodeId format](https://reference.opcfoundation.org/v104/Core/docs/Part6/5.3.1.11/) |                      |
:::

```{code-block} json
:caption: Example configuration section for OPC UA ValueProvider.
:lineno-start: 1
{
	"nodeId": "nsu=com:example;s=foo",
	"arrayIndex" : "[2]"
}
```

#### Operation Provider

:::{table} Configuration properties of OPC UA AssetConnection Operation Provider.
| Name                                   | Allowed Value                                               | Description                                                                                                                                                                                                                          | Default Value        |
| -------------------------------------- | ----------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------- |
| inputArgumentMapping<br>*(optional)*   | List                                                  | List of mappings for input arguments between the idShort of a SubmodelElement and an argument name                                                                                                                                         | *empty list*              |
| inputValidationMode<br>*(optional)*    | NONE<br>REQUIRE_PRESENT<br>REQUIRE_PRESENT_OR_DEFAULT | Validation mode for input arguments                                                                                                                                                                                                        | REQUIRE_PRESENT_OR_DEFAULT|
| inoutputValidationMode<br>*(optional)* | NONE<br>REQUIRE_PRESENT<br>REQUIRE_PRESENT_OR_DEFAULT | Validation mode for inoutput arguments                                                                                                                                                                                                     | REQUIRE_PRESENT_OR_DEFAULT|
| nodeId                                 | String                                                | NodeId of the the OPC UA node to read/write in [ExpandedNodeId format](https://reference.opcfoundation.org/v104/Core/docs/Part6/5.3.1.11/)                                                                                                    |                           |
| outputArgumentMapping<br>*(optional)*  | List                                                  | List of mappings for output arguments between the idShort of a SubmodelElement and an argument name                                                                                                                                        | *empty list*              |
| outputValidationMode<br>*(optional)*   | NONE<br>REQUIRE_PRESENT<br>REQUIRE_PRESENT_OR_DEFAULT | Validation mode for ouput arguments                                                                                                                                                                                                        | REQUIRE_PRESENT_OR_DEFAULT|
| parentNodeId<br>*(optional)*           | String                                                | NodeId of the OPC UA object in [ExpandedNodeId format](https://reference.opcfoundation.org/v104/Core/docs/Part6/5.3.1.11/), in which the method is contained.<br>When no parentNodeId is given here, the parent object of the method is used. |                           |
:::




```{code-block} json
:caption: Example configuration section for OPC UA Operation Provider.
:lineno-start: 1
{
	"nodeId": "nsu=com:example;s=foo",
	"parentNodeId": "nsu=com:example;s=fooObject",
	"inputArgumentMapping": [ {
		"idShort": "ExampleInputId",
		"argumentName": "ExampleInput"
	} ],
	"outputArgumentMapping": [ {
		"idShort": "ExampleOutputId",
		"argumentName": "ExampleOutput"
	} ]
}
```

#### Subscription Provider

:::{table} Configuration properties of OPC UA AssetConnection Subscription Provider.
| Name                       | Allowed Value | Description                                                                                                                             | Default Value        |
| -------------------------- | ------------- | --------------------------------------------------------------------------------------------------------------------------------------- | -------------------- |
| arrayIndex<br>*(optional)* | String        | Index of the desired array element if the node is an array.<br>Can be multi-dimensional.                                                |                      |
| interval                   | long          | Interval to poll the server for changes (in ms)<br>**Currently not used**                                                               | 1000                 |
| nodeId                     | String        | NodeId of the the OPC UA node to read/write in [ExpandedNodeId format](https://reference.opcfoundation.org/v104/Core/docs/Part6/5.3.1.11/) |                      |
:::

```{code-block} json
:caption: Example configuration section for OPC UA Subscription Provider.
:lineno-start: 1
{
	"nodeId": "nsu=com:example;s=foo",
	"interval": 1000,
	"arrayIndex" : "[2]"
}
```

### Complete Example

```{code-block} json
:caption: Complete example configuration section for OPC UA Asset Connection.
:lineno-start: 1
{
	"@class": "de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.OpcUaAssetConnection",
	"host": "opc.tcp://localhost:4840",
	"securityPolicy": "None",
	"securityMode" : "None",
	"applicationCertificate": {
		"keyStoreType": "PKCS12",
		"keyStorePath": "C:\faaast\MyKeyStore.p12",
		"keyStorePassword": "changeit",
		"keyAlias": "app-cert",
		"keyPassword": "changeit"
	},
	"authenticationCertificate": {
		"keyStoreType": "PKCS12",
		"keyStorePath": "C:\faaast\MyKeyStore.p12",
		"keyStorePassword": "changeit",
		"keyAlias": "auth-cert",
		"keyPassword": "changeit"
	},
	"valueProviders": {
		"[ModelRef](Submodel)urn:aas:id:example:submodel:1, (Property)Property1": {
			"nodeId": "some.node.id.property.1"
		},
		"[ModelRef](Submodel)urn:aas:id:example:submodel:1, (Property)Property2": {
			"nodeId": "some.node.id.property.2"
		}
	},
	"operationProviders": {
		"[ModelRef](Submodel)urn:aas:id:example:submodel:1, (Operation)Operation1": {
			"nodeId": "some.node.id.operation.1"
		}
	},
	"subscriptionProviders": {
		"[ModelRef](Submodel)urn:aas:id:example:submodel:1, (Property)Property3": {
			"nodeId": "some.node.id.property.3",
			"interval": 1000
		}
	}
}
```

## Lambda

The Lambda asset connection provides an easy way to create providers from code using lambda expressions.
It therefore can be used only from code and not via configuration file.

### Supported Providers

- ValueProvider
	- read ✔️
	- write ✔️
- OperationProvider ✔️
- SubscriptionProvider ✔️

### Usage

Use the `AssetConnectionManager` to un-/register lambda providers with FA³ST Service during creation of the `Service` instance.

#### Value Provider

The Lambda ValueProvider can be used to read, write, or both as shown in the following code example.

```{code-block} java
:caption: Using a Lambda ValueProvider from code.
:lineno-start: 1
Service service = new Service(...);
Reference referenceToAASElement = ...;

service.getAssetConnectionManager().registerLambdaValueProvider(
		referenceToAASElement,
		LambdaValueProvider.builder()
				.read(() -> new PropertyValue(new StringValue("example value")))
				.write(x -> System.out.println("new value: " + x))
				.build());
service.start();
```

#### Subscription Provider

The Lambda SubscriptionProvider is a bit more complex because it requires asynchronicity, i.e. running a separate thread parallel to FA³ST Service and every now and then notify FA³ST Service about new values by calling `NewDataListener.newDataReceived`.
In the example we use a `ScheduledExecutorService` to periodically call the listener but this is not required and can be done any other way.

```{code-block} java
:caption: Using a Lambda SubscriptionProvider from code.
:lineno-start: 1
Service service = new Service(...);
Reference referenceToAASElement = ...;
ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

service.getAssetConnectionManager().registerLambdaSubscriptionProvider(
		referenceToAASElement,
		LambdaSubscriptionProvider.builder()
				.generate(listener -> scheduler.scheduleAtFixedRate(
						() -> listener.newDataReceived( // periodically call listener.newDataReceived to notify FA³ST about new data
								new PropertyValue(
										new StringValue(ZonedDateTime.now().toString()))),
						0, 10, TimeUnit.SECONDS))
				.build());
service.start();
```

#### Operation Provider

The Lambda OperationProvider takes a lambda expression with two arguments, the input arguments and the inoutput arguments of the operation, and returns the result.

```{code-block} java
:caption: Using a Lambda OperationProvider from code.
:lineno-start: 1
Service service = new Service(...);
Reference referenceToAASElement = ...;

service.getAssetConnectionManager().registerLambdaOperationProvider(
		referenceToAASElement,
		LambdaOperationProvider.builder()
				.handle((input, inoutput) -> {
					// add operation logic here
					return new OperationVariable[]{};
				})
				.build());
service.start();
```