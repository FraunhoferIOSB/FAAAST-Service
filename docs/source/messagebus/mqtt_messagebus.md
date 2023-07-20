# MQTT MessageBus

This implementation of the `MessageBus` interface publishes messages via MQTT either by hosting its own MQTT server or by using an externally hosted one.

## Topics & Payload

Each message type is published on its own topic in the form if `[topicPrefix]/[className]`, e.g. `events/ValueChangeEventMessage`.
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

For derserialization of events the class `JsonEventDeserializer` in module `dataformat-json` can be used.



## Configuration


| Name | Allowed Value(s) | Description |
|:--| -- | -- |
| useInternalServer | Boolean | _optional_ If true FA³ST Service starts its own MQTT server, if false uses an external server, default: true |
| useWebsocket | Boolean | _optional_ If true uses websocket, otherwise TCP, default: false |
| port | Integer | _optional_ The port to use for TCP communication, default 1883 |
| sslPort | Integer | _optional_ The port to use for secure TCP communication, default 8883 |
| host | String | _optional_ The host name of the MQTT server (without prefix, e.g 192.168.0.1), default: localhost |
| websocketPort | Integer | _optional_ The port to use for TCP communication, default 9001 | 
| sslWebsocketPort | Integer |   _optional_ The port to use for secure websocket communication, default 443 |
| serverCertificate | Object | _optional_  The server certificate to use. If not provided, SSL will be disabled :ref:`See details<certificate-info>` |
| clientCertificate | Object | _optional_  The client certificate to use. If not provided, SSL will be disabled :ref:`See details<certificate-info>` |
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
