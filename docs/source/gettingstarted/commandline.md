# Usage with Command Line

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
3.  Execute the `.jar` file to start a FA³ST Service directly with a default configuration. Replace the `{path/to/your/AASEnvironment}` with your file to the Asset Administration Shell Environment you want to load with the FA³ST Service. If you just want to play around, you can use an example AASEnvironment from us [here](https://github.com/FraunhoferIOSB/FAAAST-Service/blob/main/misc/examples/demoAAS.json).

```sh
java -jar starter-{version}.jar -m {path/to/your/AASEnvironment}
```

Currently we supporting following formats of the Asset Administration Shell Environment model:
>json, json-ld, aml, xml, opcua nodeset, rdf


Following command line parameters are supported

| Name (short)  | Name (long)                 | Allowed Values                  | Default Value    | Description                                                                                                                                           |
| ------------- | --------------------------- | ------------------------------- | ---------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| `-c`          | `--config`                  | <file path>                     | config.json      | The config file to use.                                                                                                                               |
|               | `--emptyModel`              | <boolean>                       | false            | Starts the FAST service with an empty Asset Administration Shell Environment.                                                                         |
|               | `--endpoint`                | HTTP, OPCUA                     | <none>           | Additional endpoints that should be started.                                                                                                          |
| `-h`          | `--help`                    |                                 |                  | Print help message and exit.                                                                                                                          |
|               | `--loglevel-external`       | TRACE, DEBUG, INFO, WARN, ERROR | WARN             | Sets the log level for external packages. This overrides the log level defined by other commands such as `-q` or `-v`.                                |
|               | `--loglevel-faaast`         | TRACE, DEBUG, INFO, WARN, ERROR | WARN             | Sets the log level for FA³ST packages. This overrides the log level defined by other commands such as `-q` or `-v`.                                   |
| `-m`          | `--model`                   | <file path>                     | aasenvironment.* | The model file to load.                                                                                                                               |
|               | `--[no-]autoCompleteConfig` | <boolean>                       | true             | Autocompletes the configuration with default values for required configuration sections.                                                              |
|               | `--[no-]modelValidation`    | <boolean>                       | true             | Validates the AAS Environment.                                                                                                                        |
| `-q`          | `--quite`                   |                                 |                  | Reduces log output (ERROR for FAST packages, ERROR for all other packages). Default information about the starting process will still be printed.     |
| `-v`          | `--verbose`                 |                                 |                  | Enables verbose logging (`INFO` for FAST packages, `WARN` for all other packages).                                                                    |
| `-V`          | `--version`                 |                                 |                  | Print version information and exit.                                                                                                                   |
| `-vv`         |                             |                                 |                  | Enables very verbose logging (`DEBUG` for FAST packages, `INFO` for all other packages).                                                              |
| `-vvv`        |                             |                                 |                  | Enables very very verbose logging (`TRACE` for FAST packages, `DEBUG` for all other packages).                                                        |
|               | `<key=value>`               | any                             |                  | Additional properties to override values of configuration using JSONPath notation without starting '$.' (see https://goessner.net/articles/JsonPath/) | 

## Change the Configuration

In general there are 3 ways to configure your FA³ST Service:

1.  Default values
2.  Commandline parameters
3.  Environment Variables

The 3 kinds can be combined, e.g. by using the default configuration and customizing with commandline parameters and environment variables. If they conflict, environment variables are preferred over all and commandline parameters are preferred over the default values.

Without any manual customization a FA³ST Service with default configuration will be started. For details to the structure and components of the configuration please have a look at the configuration section.

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

The FA³ST Service Starter consider following environment variables:
-   `faaast.config` to use a own configuration file
-   `faaast.model` to use a Asset Administration Environment file

Environment variables could also be used to adjust some config components in the configuration. Therefore, we are using JSONPath notation without starting '$.' (see [here](https://goessner.net/articles/JsonPath/)) with the prefix `faaast.config.extension.`:
-   `faaast.config.extension.[dot.separated.path]`

If you want to change for example the requestHandlerThreadPoolSize in the core configuration, just set the environment variable `faaast.config.extension.core.requestHandlerThreadPoolSize=42`. To access configuration components in a list use the index. For example to change the port of the HTTP endpoint in the default configuration you can set the environment variable `faaast.config.extension.endpoints[0].port=8081`.

You could also use properties to adjust configuration components. To change the `requestHandlerThreadPoolSize` of the core component and the port of the http endpoint use

```sh
java -jar starter-{version}.jar -m {path/to/your/AASEnvironment} core.requestHandlerThreadPoolSize=42 endpoints[0].port=8081
```
