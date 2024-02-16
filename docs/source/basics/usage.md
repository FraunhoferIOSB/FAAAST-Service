# Usage

Currently, we support the following formats of the Asset Administration Shell Environment model:
>json, xml, aasx

## Command Line Interface (CLI)

```sh
cd /starter/target
java -jar starter-{version}.jar -m model.json
```

In general there are 3 ways to configure your FA³ST Service:

1.  Default values
2.  Commandline parameters
3.  Environment Variables

The 3 kinds can be combined, e.g. by using the default configuration and customizing with commandline parameters and environment variables. If they conflict, environment variables are preferred over all and commandline parameters are preferred over the default values.

Without any manual customization a FA³ST Service with default configuration will be started. For details to the structure and components see [Configuration](configuration).

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

The FA³ST Service Starter considers the following environment variables:
- `faaast_config`: use your own configuration file
- `faaast_model`: use an Asset Administration Environment file

Environment variables could also be used to adjust some config components in the configuration. Therefore, we are using JSONPath notation without starting '$.' (see [here](https://goessner.net/articles/JsonPath/)), with '_' as a separator and with the prefix `faaast_config_extension_`:
-   `faaast_config_extension_[underscore_separated_path]`

If you want to change for example the requestHandlerThreadPoolSize in the core configuration, just set the environment variable `faaast_config_extension_core_requestHandlerThreadPoolSize=42`. To access configuration components in a list use the index. For example to change the port of the HTTP endpoint in the default configuration you can set the environment variable `faaast_config_extension_endpoints[0]_port=8081`.

You could also use properties to adjust configuration components. To change the `requestHandlerThreadPoolSize` of the core component and the port of the http endpoint use

```sh
java -jar starter-{version}.jar -m {path/to/your/AASEnvironment} core.requestHandlerThreadPoolSize=42 endpoints[0].port=8081
```

### Supported Arguments


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
|               | `--no-validation`           | <boolean>                       | false            | Disables validation, overrides validation defined in the configuration Environment.                                                                                                                        |
| `-q`          | `--quite`                   |                                 |                  | Reduces log output (ERROR for FAST packages, ERROR for all other packages). Default information about the starting process will still be printed.     |
| `-v`          | `--verbose`                 |                                 |                  | Enables verbose logging (`INFO` for FAST packages, `WARN` for all other packages).                                                                    |
| `-V`          | `--version`                 |                                 |                  | Print version information and exit.                                                                                                                   |
| `-vv`         |                             |                                 |                  | Enables very verbose logging (`DEBUG` for FAST packages, `INFO` for all other packages).                                                              |
| `-vvv`        |                             |                                 |                  | Enables very very verbose logging (`TRACE` for FAST packages, `DEBUG` for all other packages).                                                        |
|               | `<key=value>`               | any                             |                  | Additional properties to override values of configuration using JSONPath notation without starting '$.' (see https://goessner.net/articles/JsonPath/) | 


## Docker

### Docker-Compose

Clone this repository, navigate to `/misc/docker/` and run this command inside it.

```sh
cd misc/docker
docker-compose up
```

To use your own AAS environment replace the model file `/misc/examples/demoAAS.json`.
To modify the configuration edit the file `/misc/examples/exampleConfiguration.json`.
You can also override configuration values using environment variables. For details see [Usage with Command Line](commandline).

### Docker CLI

To start the FA³ST Service with an empty AAS environment execute this command.

```sh
docker run --rm -P fraunhoferiosb/faaast-service '--emptyModel' '--no-modelValidation'
```

To start the FA³ST Service with your own AAS environment, place the JSON-file (in this example `demoAAS.json`) containing your environment in the current directory and modify the command accordingly.

```sh
docker run --rm -v ../examples/demoAAS.json:/AASEnv.json -e faaast.model=AASEnv.json -P fraunhoferiosb/faaast-service '--no-modelValidation'
```

Similarly, you can pass more arguments to the FA³ST service by using the CLI or a configuration file as provided in the cfg folder (use the `faaast.config` environment variable for that).


## From Java Code

```java
Service service = new Service(ServiceConfig.builder()
	.core(CoreConfig.builder()
		.requestHandlerThreadPoolSize(2)
		.build())
	.persistence(PersistenceInMemoryConfig.builder()
		.initialModelFile(new File("{pathTo}\\FAAAST-Service\\misc\\examples\\model.aasx"))
		.build())
	.endpoint(HttpEndpointConfig.builder().build())
	.messageBus(MessageBusInternalConfig.builder().build())
	.fileStorage(FileStorageInMemoryConfig.builder().build())
	.build());
service.start();
```