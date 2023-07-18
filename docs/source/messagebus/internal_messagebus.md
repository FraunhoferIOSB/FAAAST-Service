# Internal MessageBus

This is the default implementation of the `MessageBus` interface and it is implemented using Java method calls.
Therefore, it can only be accessed from code when FA³ST Service is used as an embedded library.

## Configuration

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
