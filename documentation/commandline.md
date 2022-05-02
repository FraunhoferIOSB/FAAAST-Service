## Usage with Command Line

To start a FA³ST Service from the command line:
1.  Move to the starter project and build the project
    ```sh
    cd /starter
    mvn clean package
    ```

2.  Move to the generated `.jar` file
    ```sh
    cd starter/target
    ```

3.  Execute the `.jar` file to start a FA³ST Service directly with a default configuration. Replace the `{path/to/your/AASEnvironment}` with your file to the Asset Administration Shell Environment you want to load with the FA³ST Service. If you just want to play around, you can use a example AASEnvironment from us [here](starter/src/test/resources/AASFull.json).
  ```sh
  java -jar starter-{version}.jar -m {path/to/your/AASEnvironment}
  ```

Currently we supporting following formats of the Asset Administration Shell Environment model:
>json, json-ld, aml, xml, opcua nodeset, rdf

<hr>
<p>

Following command line parameters could be used:
```
[<String=String>...]   		Additional properties to override values of configuration using
				JSONPath notation without starting '$.' (see https://goessner.net/articles/JsonPath/)

-c, --config=<configFile>  	The config file path. Default Value = config.json

--emptyModel 			Starts the FA³ST service with an empty Asset Administration Shell Environment.
				False by default

--endpoint=<endpoints>[,<endpoints>...]
				Additional endpoints that should be started.

-h, --help                 	Show this help message and exit.

-m, --model=<modelFile>    	Asset Administration Shell Environment FilePath.
				Default Value = aasenvironment.*

--[no-]autoCompleteConfig
				Autocompletes the configuration with default
				values for required configuration sections. True
				by default

--[no-]modelValidation 		Validates the AAS Environment. True by default

-V, --version              	Print version information and exit.
```
<hr>
<p>

### Change the Configuration
<p>

In general there are 3 ways to configure your FA³ST Service:
1.  Default values
2.  Commandline parameters
3.  Environment Variables

The 3 kinds can be combined, e.g. by using the default configuration and customizing with commandline parameters and environment variables. If they conflict, environment variables are preferred over all and commandline parameters are preferred over the default values.

Without any manual customization a FA³ST Service with default configuration will be started. For details to the structure and components of the configuration please have a look at the configuration section [here]()

Default Configuration:
```json
{
	"core" : {
		"requestHandlerThreadPoolSize" : 2
	},
	"endpoints" : [ {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint",
		"port" : 8080
	} ],
	"persistence" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemory"
	},
	"messageBus" : {
		"@class" : "de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternal"
	}
}
```
<hr>
<p>

The FA³ST Service Starter consider following environment variables:
-   `faaast.config` to use a own configuration file
-   `faaast.model` to use a Asset Administration Environment file

Environment variables could also be used to adjust some config components in the configuration. Therefore, we are using JSONPath notation without starting '$.' (see [here](https://goessner.net/articles/JsonPath/)) with the prefix `faaast.config.extension.`:
-   `faaast.config.extension.[dot.separated.path]`

If you want to change for example the requestHandlerThreadPoolSize in the core configuration, just set the environment variable `faaast.config.extension.core.requestHandlerThreadPoolSize=42`. To access configuration components in a list use the index. For example to change the port of the HTTP endpoint in the default configuration you can set the environment variable `faaast.config.extension.endpoints[0].port=8081`.

<hr>
<p>

You could also use properties to adjust configuration components. To change the `requestHandlerThreadPoolSize` of the core component and the port of the http endpoint use
```sh
java -jar starter-{version}.jar -m {path/to/your/AASEnvironment}
core.requestHandlerThreadPoolSize=42 endpoints[0].port=8081
```
<hr>
<p>

### Special Parameters

The parameter `--endpoint` accepts a list of endpoints which should be started with the service. Currently supported is `http` and `opcua`. So a execution of
```sh
java -jar starter-{version}.jar -m {path/to/your/AASEnvironment} --endoint http
```
leads to a FA³ST Service with the HTTP endpoint implemented in class `de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint`.
