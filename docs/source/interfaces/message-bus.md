# Message Bus

In FA³ST Service, `MessageBus` is used for communication between different components, for example to synchronize between endpoints using different protocols.
Therefore, the MessageBus is primarily designed for internal use but as it might also be useful for some applications and scenarios there might be implementations that expose the MessageBus to the outside world.

## Events

The `MessageBus` works according to the publish/subscribe principle based on different types of events or event messages (which are subclasses of the abstract class `EventMessage`).
Subscriptions are made to a kind of event, i.e. a subclass of `EventMessage` or even `EventMessage` itself (to receive all events).
When subscribing to a class, all events of this class or any subclass are received.

This is the class hierarchy of available event classes/types

*   `EventMessage`	[abstract]							Superclass for all events, payload: a `Reference` to the subject element
	*   `AccessEventMessage` [abstract]					Superclass for all types of access-based events
		*   `ReadEventMessage` [abstract]				Superclass for all types of read-events, triggered each time an element is read via API
			*   `ElementReadEventMessage`				Triggered when a `Referable` is read via API, payload: the referable (serialized according to the request, i.e. using the requested `SerializationModifier`)
			*   `ValueReadEventMessage`					Triggered when the value of an element is read via API, payload: the element value
		*   `ExecuteEventMessage` [abstract]			Superclass for all events related to executing operations
			*   `OperationInvokeEventMessage`			Triggered when an operation is invoked/started, payload: input and inoutput parameters
			*   `OperationFinishEventMessage`			Triggered when an operation is finished, payload: output and inoutput parameters
	*   `ChangeEventMessage` [abstract]					Superclass for all types of changes
		*   `ElementChangeEventMessage` [abstract]		Superclass for all types of structural changes, payload: the updated element
			*   `ElementCreateEventMessage`				Triggered when an element is created
			*   `ElementDeleteEventMessage`				Triggered when an element is deleted
			*   `ElementUpdateEventMessage`				Triggered when an element is updated
			*   `ValueChangeEventMessage`				Triggered when the value of an element is updated, payload: old value, new value
	*   `ErrorEventMessage`								Triggered when an error occurred, payload: message, error level (INFO, WARN, ERROR)


## Internal

This is the default implementation of the MessageBus interface which is implemented using Java method calls.
Therefore, it can only be accessed from code when FA³ST Service is used as an embedded library.

### Configuration

This class does not offer any configuration properties.


#### Example

```json
{
	...,
	"messageBus":
	{
		"@class": "de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternal"
	},
	...
}
```

## MQTT

This implementation of the `MessageBus` interface publishes messages via MQTT either by hosting its own MQTT server or by using an externally hosted one.

### Topics & Payload

Each message type is published on its own topic in the form of `[topicPrefix]/[className]`, e.g. `events/ValueChangeEventMessage`.
The payload is a JSON serialization of the corresponding Java class with the following base structure

```json
{
	"@type": "[event type]",
	"element": { [default JSON serialization of Reference] },
	[event-specific properties]
}
```

An example `ValueChangeEvent` might look like this
```json
{
    "@type": "ValueChangeEvent",
    "element":
    {
        "keys": [
            {
                "idType": "Iri",
                "type": "Submodel",
                "value": "http://example.org/submodel"
            },
            {
                "idType": "IdShort",
                "type": "Property",
                "value": "property"
            }
        ]
    },
    "oldValue":
    {
        "modelType": "Property",
        "dataType": "int",
        "value": 0
    },
    "newValue":
    {
        "modelType": "Property",
        "dataType": "int",
        "value": 1
    }
}

```

For deserialization of events the class `JsonEventDeserializer` in module `dataformat-json` can be used.



### Configuration


| Name | Allowed Value(s) | Description |
|:--| -- | -- |
| useInternalServer | Boolean | _optional_ If true FA³ST Service starts its own MQTT server, if false uses an external server, default: true |
| useWebsocket | Boolean | _optional_ If true uses websocket, otherwise TCP, default: false |
| port | Integer | _optional_ The port to use for TCP communication, default 1883 |
| sslPort | Integer | _optional_ The port to use for secure TCP communication, default 8883 |
| host | String | _optional_ The host name of the MQTT server (without prefix, e.g 192.168.0.1), default: localhost |
| websocketPort | Integer | _optional_ The port to use for TCP communication, default 9001 | 
| sslWebsocketPort | Integer |   _optional_ The port to use for secure websocket communication, default 443 |
| serverCertificate | Object | _optional_  The server certificate to use. If not provided, SSL will be disabled [See details](../../gettingstarted/configuration#providing-certificates-in-configuration) |
| clientCertificate | Object | _optional_  The client certificate to use. If not provided, SSL will be disabled [See details](../../gettingstarted/configuration#providing-certificates-in-configuration) |
| users | Map | _optional_ Map of usernames and passwords of users that are allowed to connect to the MQTT server. This is only used when `useInternalServer` is true, default: empty |
| username | String | _optional_ Username used to connect to the MQTT server |
| password | String | _optional_ Password used to connect to the MQTT server |
| clientId | String | _optional_ ClientId to use when connecting to the MQTT server, default: FAST MQTT MessageBus |
| topicPrefix | String | _optional_ Prefix to use for the topic names, default: events/ |


#### Example

```json
{
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
}
```
