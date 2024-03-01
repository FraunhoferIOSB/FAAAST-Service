# Usage

## Command-Line Interface (CLI)

To start FA³ST Service from command-line you need to run the `starter` module by calling

```sh
> java -jar starter-{version}.jar
```

When started without arguments, FA³ST Service will try to auto-detect a configuration file named `config.json` and a model file named `model.[ext]` where `[ext]` is a supported file extension like `json`, `xml`, or `aasx`.

To manually pass a model file `my-model.aasx` and a configuration file `my-config.json` run the following command:

```sh
> java -jar starter-{version}.jar --model my-model.aasx --config my-config.json
```

:::{table} Supported CLI arguments and environment variables.
| CLI (short) | CLI (long)          | Environment variable                                           | Allowed<br>Values                       | Description                                                                                                                                              | Default<br>Value |
| ----------- | ------------------- | -------------------------------------------------------------- | --------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------- |
| -c          | --config            | faaast_config                                                  | <file path>                             | The config file to use.                                                                                                                                  | config.json      |
| -e          | --empty-model       |                                                                |                                         | Starts the FAST service with an empty Asset Administration Shell Environment.                                                                            |                  |
|             | --endpoint          |                                                                | HTTP<br>OPCUA                           | Additional endpoints that should be started.                                                                                                             |                  |
| -h          | --help              |                                                                |                                         | Print help message and exit.                                                                                                                             |                  | 
|             | --loglevel-external | faaast_loglevel_external                                       | TRACE<br>DEBUG<br>INFO<br>WARN<br>ERROR | Sets the log level for external packages.<br>This overrides the log level defined by other commands such as *-q* or *-v*.                                | WARN             |
|             | --loglevel-faaast   | faaast_loglevel_faaast                                         | TRACE<br>DEBUG<br>INFO<br>WARN<br>ERROR | Sets the log level for FA³ST packages.<br>This overrides the log level defined by other commands such as *-q* or *-v*.                                   | WARN             |
| -m          | --model             |                                                                | <file path>                             | The model file to load.                                                                                                                                  | model.*          |
|             | --no-validation     | faaast_no_validation                                           |                                         | Disables all validation, overrides validation defined in the configuration Environment.                                                                  |                  |
| -q          | --quite             |                                                                |                                         | Reduces log output (*ERROR* for FAST packages, *ERROR* for all other packages).<br>Default information about the starting process will still be printed. |                  |
| -v          | --verbose           |                                                                |                                         | Enables verbose logging (*INFO* for FAST packages, *WARN* for all other packages).                                                                       |                  |
| -V          | --version           |                                                                |                                         | Print version information and exit.                                                                                                                      |                  |
| -vv         |                     |                                                                |                                         | Enables very verbose logging (*DEBUG* for FAST packages, *INFO* for all other packages).                                                                 |                  |
| -vvv        |                     |                                                                |                                         | Enables very very verbose logging (*TRACE* for FAST packages, *DEBUG* for all other packages).                                                           |                  |
|             | {key}={value}       | faaast_config_extension_{key}<br>with *{key}* separated by *_* | any                                     | Additional properties to override values of configuration using [JSONPath](https://goessner.net/articles/JsonPath/) notation without starting *$.*       |                  |
:::

### Overriding Config Properties

As indicated by the last row in the above table, any config property can be overridden both via CLI or via environment variables.

#### Via CLI

Via CLI this is done by using the JSONPath expression to the property within the config file but without the `$.` part JSONPath expression typically start with.

For example, to override the `requestHandlerThreadPoolSize` property call FA³ST Service like this

```sh
> java -jar starter-{version}.jar [any other CLI arguments] core.requestHandlerThreadPoolSize=42
```

To access configuration properties inside an array or list use array notation, e.g., `endpoints[0].port=8081`


#### Via Environment Variables

Overriding configuration properties via environment variables is similar to overriding them via CLI with two differences

1. Add the prefix *faaast_config_extension_*
2. Replace `.` that separate the JSONPath with `_`

Applying the previous examples yields `faaast_config_extension_core_requestHandlerThreadPoolSize=42` to update the property `requestHandlerThreadPoolSize` and `faaast_config_extension_endpoints[0]_port=8081` to update the port of the HTTP endpoint.


## Docker

FA³ST Service is available on [DockerHub](https://hub.docker.com/r/fraunhoferiosb/faaast-service) with multiple tags

- `latest`: The latests released version, equals to the latests tag `major.minor.bugfix`
- `major.minor.0-SNAPSHOT`: Snapshot build of the current code on the `main` branch of FA³ST Service. This includes all upcoming features not yet relased.
- `major.minor.bugfix`: This tag is available for each officially released version of FA³ST. It is stable, i.e., no updates or bugfixes will ever be applied.
- `major.minor`: This tag is available for each minor release of FA³ST Service and will be updated with bugfixes over time. It is therefore recommended to use these tags over the `major.minor.bugfix` ones.

To run FA³ST Service via docker with an empty model and default configuration execute

```sh
> docker run fraunhoferiosb/faaast-service
```

To make you of the full power of docker and FA³ST Service, you can also mount files to the container and pass arguments via CLI or environment variables like this

```sh
> docker run -v {path to your model file}:/model.json -e faaast.model=model.json fraunhoferiosb/faaast-service '--no-validation'
```

FA³ST Service also comes with a docker compose file located at `/misc/docker/docker-compose.yml` which can be executed by navigation to the directory `/misc/docker` and execute `docker-compose up`.


## From Java Code

You can run FA³ST Service directly from your Java code as embedded library.
This way, you can create your configuration and model directly in code and don't have to create them as files (you can still load them from files if you want to).
The following code snippet shows how to create and run a new FA³ST Service from code using a model file.

```{code-block} java
:caption: Create a FA³ST Service from code.
:lineno-start: 1
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