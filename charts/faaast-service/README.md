# helm-faaast-service

# FA³ST Service [![Documentation Status](https://readthedocs.org/projects/faaast-service/badge/?version=latest)](https://faaast-service.readthedocs.io/en/latest/?badge=latest)

![FA³ST Service Logo Light](https://github.com/FraunhoferIOSB/FAAAST-Service/blob/main/docs/source/images/logo-positiv.png/#gh-light-mode-only "FA³ST Service Logo")
![FA³ST Service Logo Dark](https://github.com/FraunhoferIOSB/FAAAST-Service/blob/main/docs/source/images/logo-negativ.png/#gh-dark-mode-only "FA³ST Service Logo")

Helm chart for the **F**raunhofer **A**dvanced **A**sset **A**dministration **S**hell **T**ools (**FA³ST**) Service.

For more details on FA³ST Service see the full documentation :blue_book: [here](https://faaast-service.readthedocs.io/).

Repository: https://github.com/FraunhoferIOSB/FAAAST-Service

Dockerhub: https://hub.docker.com/r/fraunhoferiosb/faaast-service

## Features

### Registry Integration

This Helm chart includes integration with **FA³ST Registry** following the **Plattform Industrie 4.0** specification.

The FA³ST Service automatically synchronizes Asset Administration Shells (AAS) and Submodels with a configured registry:

- **Automatic Registration**: New AAS and Submodels are automatically registered when created
- **Automatic Updates**: Changes to AAS and Submodels are automatically synchronized
- **Automatic Cleanup**: AAS and Submodels are unregistered when deleted or on service shutdown
- **Cloud Events**: Integration uses Cloud Events over MQTT for create and update events

**The RegistryClient communicates with an AAS-Registry following Plattform Industrie 4.0 standards.**

For detailed testing instructions, see [TESTING_GUIDE.md](./TESTING_GUIDE.md).


## Configuration


To configure the FA³ST Service, please refer to the [docs](https://faaast-service.readthedocs.io/). Configurations in
helm are translated as in this example:
```json
{
  "endpoints": [
    {
      "@class": "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint",
      "port": 443
    },
    {
      "@class": "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint",
      "port": 8080,
      "sslEnabled": false
    }
  ]
}
```
```yaml
endpoints:
  - class: de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint
    port: 443
  - class: de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint
    port: 8080
    sslEnabled: false
```

### API Endpoints

The registry integration uses the following standard APIs:

- **AAS Registry**: `/api/v3.0/shell-descriptors`
- **Submodel Registry**: `/api/v3.0/submodel-descriptors`

For API documentation, see:
- [AAS Repository API](https://factory-x-contributions.github.io/async-aas-helm/aas-repository/)
- [Submodel Repository API](https://factory-x-contributions.github.io/async-aas-helm/submodel-repository/)

## Installation

```bash
helm install faaast-service ./charts/faaast-service \
  --set registry.aasRegistryBaseUrl="https://faaast-registry" \
  --set registry.submodelRegistryBaseUrl="https://faaast-registry"

# If you need to call the Registry via ingress instead, override to your ingress URL:
helm install faaast-service ./charts/faaast-service \
  --set registry.aasRegistryBaseUrl="https://faaast-registry.factory-x.catena-x.net" \
  --set registry.submodelRegistryBaseUrl="https://faaast-registry.factory-x.catena-x.net"
```
