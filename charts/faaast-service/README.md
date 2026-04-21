# FA³ST Service [![Documentation Status](https://readthedocs.org/projects/faaast-service/badge/?version=latest)](https://faaast-service.readthedocs.io/en/latest/?badge=latest)

![FA³ST Service Logo Light](https://github.com/FraunhoferIOSB/FAAAST-Service/blob/main/docs/source/images/logo-positiv.png/#gh-light-mode-only "FA³ST Service Logo")
![FA³ST Service Logo Dark](https://github.com/FraunhoferIOSB/FAAAST-Service/blob/main/docs/source/images/logo-negativ.png/#gh-dark-mode-only "FA³ST Service Logo")

Helm chart for the **F**raunhofer **A**dvanced **A**sset **A**dministration **S**hell **T**ools (**FA³ST**) Service.

For more details on FA³ST Service see the full documentation :blue_book: [here](https://faaast-service.readthedocs.io/).

Repository: <https://github.com/FraunhoferIOSB/FAAAST-Service>

Dockerhub: <https://hub.docker.com/r/fraunhoferiosb/faaast-service>

## Resources

this chart contains two `values.yaml` files.

- `values.yaml`: The default values without any custom configuration. Any FA³ST / Kubernetes configuration can be dropped into this file (see also the next section on converting a FA³ST configuration from JSON to YAML).
- `values.full.yaml`: A values file with most/all possible configuration values. This will not work out-of-the-box. For example, the certificate files need to be mounted from the importing chart directory into the deployment by defining volumes and volumeMounts in the `values.yaml`.

### Configuring FA³ST

If you already have a FA³ST Service configuration in JSON format, you can convert it into YAML using this command:

`FILE=<path_to_your_file> && python3 -c "import yaml, json; print(yaml.dump(json.load(open(\"$FILE\"))))"`

and paste the result into your `values.yaml`.

Alternatively, directly append the resulting YAML configuration to your `values.yaml`:

`FILE=<path_to_your_file> && python3 -c "import yaml, json; print(yaml.dump(json.load(open(\"$FILE\"))))" >> values.noconfig.yaml`

#### FA³ST Security

To use FA³ST with AAS security enabled, `aclFolder` needs to be defined in the values.yaml (check the FA³ST docs for proper configuration). `aclFolder` needs to point to the place **within the chart directory importing the FA³ST service chart**, where the ACL rules (JSON-Files) are stored. For example, if your ACL rules are stored under `<your-chart>/acl/my-rules`, `.Values.endpoints[i].aclFolder` must be set to `acl/my-rules`. The FA³ST Service chart will then find those files and mount them into the container at the appropriate location.
