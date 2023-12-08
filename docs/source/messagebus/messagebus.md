# MessageBus

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
