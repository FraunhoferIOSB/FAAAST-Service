# Usage with Docker

This section describes the usage with docker and docker compose.

## Docker-Compose

Clone this repository, navigate to `/misc/docker/` and run this command inside it.

```sh
cd misc/docker
docker-compose up
```

To use your own AAS environment replace the model file `/misc/examples/demoAAS.json`.
To modify the configuration edit the file `/misc/examples/exampleConfiguration.json`.
You can also override configuration values using environment variables. For details have a look into the commandline section.

## Docker CLI

To start the FA³ST Service with an empty AAS environment execute this command.

```sh
docker run --rm -P fraunhoferiosb/faaast-service '--emptyModel' '--no-modelValidation'
```

To start the FA³ST Service with your own AAS environment, place the JSON-file (in this example `demoAAS.json`) containing your enviroment in the current directory and modify the command accordingly.

```sh
docker run --rm -v ../examples/demoAAS.json:/AASEnv.json -e faaast.model=AASEnv.json -P fraunhoferiosb/faaast-service '--no-modelValidation'
```

Similarly to the above examples you can pass more arguments to the FA³ST service by using the CLI or a configuration file as provided in the cfg folder (use the `faaast.config` environment variable for that).
