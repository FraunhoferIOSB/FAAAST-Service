# MessageBus

The MessageBus interface is used for communication between different components, for example to synchronize between endpoints.
Therefore, the MessageBus is primarily designed for internal use but as it might also be useful for some applications and scenarios there might be implementations that expose the MessageBus to the outside world.

## Events

The MessageBus works according to the publish/subscribe principle based on different types of events or event messages (which are subclasses of the abstract class `EventMessage`).
Subscriptions are made to a kind of event, i.e. a subclass of `EventMessage` or even `EventMessage` itself (to receive all events).
When subscribing to a class, all events of this class or any subclass are received.

This is the class hierarchy of available event classes/types

- `EventMessage` *(abstract)*:						Superclass for all events, payload: a `Reference` to the subject element
	- `AccessEventMessage` *(abstract)*:			Superclass for all types of access-based events
		- `ReadEventMessage` *(abstract)*:			Superclass for all types of read-events, triggered each time an element is read via API
			- `ElementReadEventMessage`:			Triggered when a `Referable` is read via API, payload: the referable (serialized according to the request, i.e. using the requested `SerializationModifier`)
			- `ValueReadEventMessage`:				Triggered when the value of an element is read via API, payload: the element value
		- `ExecuteEventMessage` *(abstract)*:		Superclass for all events related to executing operations
			- `OperationInvokeEventMessage`:		Triggered when an operation is invoked/started, payload: input and inoutput parameters
			- `OperationFinishEventMessage`:		Triggered when an operation is finished, payload: output and inoutput parameters
	- `ChangeEventMessage` *(abstract)*:			Superclass for all types of changes
		- `ElementChangeEventMessage` *(abstract)*:	Superclass for all types of structural changes, payload: the updated element
			- `ElementCreateEventMessage`:			Triggered when an element is created
			- `ElementDeleteEventMessage`:			Triggered when an element is deleted
			- `ElementUpdateEventMessage`:			Triggered when an element is updated
		- `ValueChangeEventMessage`:			Triggered when the value of an element is updated, payload: old value, new value
	- `ErrorEventMessage`:							Triggered when an error occurred, payload: message, error level (INFO, WARN, ERROR)


## Internal

This is the default implementation of the MessageBus interface which is implemented using Java method calls.
Therefore, it can only be accessed from code when FA³ST Service is used as an embedded library.

### Configuration

This implementation does not offer any configuration properties.

```{code-block} json
:caption: Example configuration for Internal MessageBus.
:lineno-start: 1
{
	"messageBus": {
		"@class": "de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternal"
	},
	//...
}
```

## MQTT

This implementation of the `MessageBus` interface publishes messages via MQTT either by hosting its own MQTT server or by using an externally hosted one.

### Topics & Payload

Each message type is published on its own topic in the form of `[topicPrefix]/[className]`, e.g. `events/ValueChangeEventMessage`.
The payload is a JSON serialization of the corresponding Java class with the following base structure

```{code-block} json
:caption: JSON structure of serialized MessageBus events.
:lineno-start: 1
{
	"@type": "[event type]",
	"element": { 
		// [default JSON serialization of Reference] 
	},
	// [event-specific properties]
}
```

An example `ValueChangeEvent` might look like this:

```{code-block} json
:caption: JSON serialization of an example ValueChangeEvent.
:lineno-start: 1
{
    "@type": "ValueChangeEvent",
    "element": {
        "keys": [
            {
                "type": "Submodel",
                "value": "http://example.org/submodel"
            },
            {
                "type": "Property",
                "value": "property"
            }
    ] },
    "oldValue": {
        "modelType": "Property",
        "dataType": "xs:int",
        "value": 0
    },
    "newValue": {
        "modelType": "Property",
        "dataType": "xs:int",
        "value": 1
    }
}
```

For deserialization of events the class `JsonEventDeserializer` in module `dataformat-json` can be used.


### Configuration

:::{table} Configuration properties of MQTT MessageBus.
| Name                              | Allowed Value                                               | Description                                                                                                                                   | Default Value              |
| --------------------------------- | ----------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------- |
| clientCertificate<br>*(optional)* | [CertificateInfo](#providing-certificates-in-configuration) | The client certificate to use. If not set, SSL will be disabled.                                                                              |                            |
| clientId<br>*(optional)*          | String                                                      | ClientId to use when connecting to the MQTT server.                                                                                           | FAST MQTT MessageBus       |
| host<br>*(optional)*              | String                                                      | The host name of the MQTT server without prefix, e.g., 192.168.0.1.                                                                           | localhost                  |
| password<br>*(optional)*          | String                                                      | Password used to connect to the MQTT server.                                                                                                  |                            |
| port<br>*(optional)*              | Integer                                                     | The port to use for TCP communication.                                                                                                        | 1883                       |
| serverCertificate<br>*(optional)* | [CertificateInfo](#providing-certificates-in-configuration) | The server certificate to use. If not set, SSL will be disabled.                                                                              |                            |
| sslPort<br>*(optional)*           | Integer                                                     | The port to use for secure TCP communication.                                                                                                 | 8883                       |
| sslWebsocketPort<br>*(optional)*  | Integer                                                     | The port to use for secure websocket communication.                                                                                           | 443                        |
| topicPrefix<br>*(optional)*       | String                                                      | Prefix to use for the topic names.                                                                                                            | events/                    |
| useInternalServer<br>*(optional)* | Boolean                                                     | If true, FA³ST Service starts its own MQTT server.<br>If false, FA³ST Service uses external MQTT server.                                      | true                       |
| username<br>*(optional)*          | String                                                      | Username used to connect to the MQTT server.                                                                                                  |                            |
| users<br>*(optional)*             | Map<String, String>                                         | Map of usernames and passwords of users that are allowed to connect to the MQTT server.<br>This is only used when `useInternalServer` is true | *empty list*               |
| useWebsocket<br>*(optional)*      | Boolean                                                     | If true uses websocket, otherwise TCP.                                                                                                        | false                      |
| websocketPort<br>*(optional)*     | Integer                                                     | The port to use for TCP communication                                                                                                         | 9001                       |
:::

```{code-block} json
:caption: Example configuration for MQTT MessageBus.
:lineno-start: 1
{
	"messageBus": {
		"@class": "de.fraunhofer.iosb.ilt.faaast.service.messagebus.mqtt.MessageBusMqtt",
		"useInternalServer": true,
		"port": 1883,
		"sslPort": 8883,
		"host": "localhost",
		"websocketPort": 9001,
		"sslWebsocketPort": 443,
		"serverCertificate": {
			"keyStoreType": "PKCS12",
			"keyStorePath": "C:\faaast\MyKeyStore.p12",
			"keyStorePassword": "changeit",
			"keyAlias": "server-key",
			"keyPassword": "changeit"
		},
		"clientCertificate": {
			"keyStoreType": "PKCS12",
			"keyStorePath": "C:\faaast\MyKeyStore.p12",
			"keyStorePassword": "changeit",
			"keyAlias": "client-key",
			"keyPassword": "changeit"
		},
		"users": {
			"user1": "password1"
		},
		"username": "messagebus-user",
		"password": "messagebus-password",
		"clientId": "CustomClientId",
		"topicPrefix": "faaast/events/"
	},
	//...
}
```



## CloudEvents

This implementation of the `MessageBus` interface publishes CloudEvent messages via MQTT by using an externally hosted MQTT broker.

### Topics & Payload

Each message type is published on its own topic in the form of `[topicPrefix]`.
The payload is a JSON serialization of a CloudEvent as specified in the async-aas specification: https://factory-x-contributions.github.io/async-aas-helm

### Configuration

:::{table} Configuration properties of MQTT MessageBus.
| Name                                | Allowed Value                                               | Description                                                                                                                                                     | Default Value                                                                                        |
| ----------------------------------- | ----------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| host<br>                            | String                                                      | The host name of the MQTT server with scheme, port and path segments if relevant.                                                                               |                                                                                                      |
| clientId<br>*(optional)*            | String                                                      | ClientId to use when connecting to the MQTT server. This clientId will only be used to identify as a MQTT publisher, not for oauth-flows or as a username.      | FA3ST MQTT MessageBus                                                                                |
| username<br>*(optional)*            | String                                                      | Username used to connect to the MQTT server.                                                                                                                    |                                                                                                      |
| password<br>*(optional)*            | String                                                      | Password used to connect to the MQTT server.                                                                                                                    |                                                                                                      |
| identityProviderUrl<br>*(optional)* | String                                                      | Oauth2 IdP URL. Obtained tokens are placed as the MQTT broker password, the username is obtained from the username config. Token refresh happens automatically. |                                                                                                      |
| oauth2ClientId<br>*(optional)*      | String                                                      | Oauth2 client id to use when retrieving an oauth2-token from the IdP.                                                                                           |                                                                                                      |
| oauth2ClientSecret<br>*(optional)*  | String                                                      | Oauth2 client secret to use when retrieving an oauth2-token from the IdP.                                                                                       |                                                                                                      |
| topicPrefix<br>*(optional)*         | String                                                      | Prefix to use for the topic names.                                                                                                                              | noauth                                                                                               |
| slimEvents<br>*(optional)*          | boolean                                                     | Whether the full referable is sent in a CloudEvent's data-field. If true, the data-field will be empty.                                                         | true                                                                                                 |
| eventTypePrefix<br>*(optional)*     | String                                                      | Prefix to use in the CloudEvents type-field. Should end with a ".".                                                                                             | io.admin-shell.events.v1.                                                                            |
| dataSchemaPrefix<br>*(optional)*    | String                                                      | Prefix to use in the CloudEvents dataschema-field. Should end with a "/".                                                                                       | https://api.swaggerhub.com/domains/Plattform_i40/Part1-MetaModel-Schemas/V3.1.0#/components/schemas/ |
| clientCertificate<br>*(optional)*   | [CertificateInfo](#providing-certificates-in-configuration) | The client certificate to use. If not set, SSL will be disabled.                                                                                                |                                                                                                      |
:::

```{code-block} json
:caption: Example configuration for MQTT MessageBus.
:lineno-start: 1
{
	"messageBus": {
		"@class": "de.fraunhofer.iosb.ilt.faaast.service.messagebus.mqtt.MessageBusMqtt",
		"useInternalServer": true,
		"host": "tcp://localhost:1883",
		"clientCertificate": {
			"keyStoreType": "PKCS12",
			"keyStorePath": "C:\faaast\MyKeyStore.p12",
			"keyStorePassword": "changeit",
			"keyAlias": "client-key",
			"keyPassword": "changeit"
		},
		"username": "broker-user",
		"password": "broker-password",
		"identityProviderUrl": "http://localhost:12345/realms/fa3st/protocol/openid-connect/token",
		"oauth2ClientId": "my-keycloak-clientid",
		"oauth2ClientSecret": "my-keycloak-clientsecret",
		"clientId": "FA³ST Service client",
		"topicPrefix": "noauth",
		"slimEvents": true,
		"eventTypePrefix": "io.admin-shell.events.v1.",
		"dataSchemaPrefix": "https://api.swaggerhub.com/domains/Plattform_i40/Part1-MetaModel-Schemas/V3.1.0#/components/schemas/"
	},
	//...
}
```
